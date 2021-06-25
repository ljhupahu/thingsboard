#!/bin/bash
#
# Copyright © 2016-2021 The Thingsboard Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 安装数据库并执行数据库初始化操作及spring环境信息
#

function installTb() {

    loadDemo=$1
    #设置node节点的启动配置和日志配置信息
    kubectl apply -f common/tb-node-configmap.yml
    #执行ThingsboardInstallApplication逻辑
    kubectl apply -f common/database-setup.yml &&
    kubectl wait --for=condition=Ready pod/tb-db-setup --timeout=120s &&
    kubectl exec tb-db-setup -- sh -c 'export INSTALL_TB=true; export LOAD_DEMO='"$loadDemo"'; start-tb-node.sh; touch /tmp/install-finished;'

    kubectl delete pod tb-db-setup

}
#安装postgres
function installPostgres() {

    #部署postgres
    kubectl apply -f common/postgres.yml
    #部署关于数据库的配置信息
    kubectl apply -f common/tb-node-postgres-configmap.yml
    #查看 postgres部署状态
    kubectl rollout status deployment/postgres
}
#安装postgres和cassandra
function installHybrid() {

    kubectl apply -f common/postgres.yml
    kubectl apply -f common/cassandra.yml
    kubectl apply -f common/tb-node-hybrid-configmap.yml

    kubectl rollout status deployment/postgres
    kubectl rollout status statefulset/cassandra

    kubectl exec -it cassandra-0 -- bash -c "cqlsh -e \
                    \"CREATE KEYSPACE IF NOT EXISTS thingsboard \
                    WITH replication = { \
                        'class' : 'SimpleStrategy', \
                        'replication_factor' : 1 \
                    };\""
}

##当传递的脚本个数大于0的时候
while [[ $# -gt 0 ]]
do
##取第一个参数等于key
key="$1"

case $key in
    --loadDemo)
    LOAD_DEMO=true
    shift # past argument
    ;;
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done

if [ "$LOAD_DEMO" == "true" ]; then
    loadDemo=true
else
    loadDemo=false
fi
#设定环境变量,决定部署方式
source .env
#k8s中新建命名空间 thingsboard
kubectl apply -f common/tb-namespace.yml
#设置k8s环境变量
kubectl config set-context $(kubectl config current-context) --namespace=thingsboard
#校验在.env中定义的部署类型
case $DEPLOYMENT_TYPE in
        basic) #基础部署
        ;;
        high-availability) #高可用部署
        ;;
        *)
        #如果部署类型配置的不对的话,输出到标准错误输出流中
        echo "Unknown DEPLOYMENT_TYPE value specified: '${DEPLOYMENT_TYPE}'. Should be either basic or high-availability." >&2
        exit 1
esac
#执行数据库部署
case $DATABASE in
        postgres)
            installPostgres
            installTb ${loadDemo}
        ;;
        hybrid)
            installHybrid
            installTb ${loadDemo}
        ;;
        *)
        echo "Unknown DATABASE value specified: '${DATABASE}'. Should be either postgres or hybrid." >&2
        exit 1
esac

