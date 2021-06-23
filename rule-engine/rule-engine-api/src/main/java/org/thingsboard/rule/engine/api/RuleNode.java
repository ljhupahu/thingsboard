/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.rule.engine.api;

import org.thingsboard.server.common.data.plugin.ComponentScope;
import org.thingsboard.server.common.data.plugin.ComponentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注释定义了节点类型、名称、描述、UI 形式和输出
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RuleNode {

    //指定规则链编辑器的哪个部分将包含此规则节点， 在规则链中有以下的 ENRICHMENT, FILTER, TRANSFORMATION, ACTION, EXTERNAL 规则部分
    ComponentType type();

    //规则节点名称
    String name();

    //节点的简短描述。在规则链编辑器中可见
    String nodeDescription();

    //带有 html 标签支持的节点的完整描述。在规则链编辑器中可见；
    String nodeDetails();

    //描述配置 json 的类的完整类名。
    Class<? extends NodeConfiguration> configClazz();

    boolean inEnabled() default true;

    boolean outEnabled() default true;

    //节点所应用的范围
    ComponentScope scope() default ComponentScope.TENANT;

    // 具有预定义关系类型的字符串数组；此值应对应于TbContext.tellNext方法中使用的值；比如TbCheckAlarmStatusNode中的true, false
    String[] relationTypes() default {"Success", "Failure"};

    //包含配置指令的 Angular UI 文件的路径。可选，可能为空。在这种情况下，用户将看到原始 JSON 编辑器；
    String[] uiResources() default {};

    //基于  UI 指令的Angular名称，允许用户编辑规则节点的配置。可选，可能为空。在这种情况下，用户将看到原始 SON 编辑器；
    String configDirective() default "";

    // 图标名称
    String icon() default "";

    // 用于在位于规则链编辑器中的节点列表中显示规则节点的图标的完整 URL；
    String iconUrl() default "";

    //链接到将在规则链编辑器中可用的当前规则节点的文档页面
    String docUrl() default "";

    boolean customRelations() default false;

}
