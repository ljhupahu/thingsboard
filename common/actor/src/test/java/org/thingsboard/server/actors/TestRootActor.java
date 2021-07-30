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
package org.thingsboard.server.actors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.common.msg.TbActorMsg;

@Slf4j
public class TestRootActor extends AbstractTbActor {

    @Getter
    private final TbActorId actorId;
    @Getter
    private final ActorTestCtx testCtx;

    private boolean initialized;
    private long sum;
    private int count;

    public TestRootActor(TbActorId actorId, ActorTestCtx testCtx) {
        this.actorId = actorId;
        this.testCtx = testCtx;
    }

    @Override
    public void init(TbActorCtx ctx) throws TbActorException {
        super.init(ctx);
        initialized = true;
    }

    /**
     * 由于是取队列中的数据进行消费，队列有顺序，所以不可能有多个线程同时对这里进行处理
     * @param msg
     * @return
     */
    @Override
    public boolean process(TbActorMsg msg) {
        log.info("线程:{} 处理消息--:{}",Thread.currentThread().getName(),((IntTbActorMsg) msg).getValue());
        if (initialized) {
            int value = ((IntTbActorMsg) msg).getValue();
            sum += value;
            count += 1;
            log.info("\033[33;4m"+"count = {}"+"\033[0m",count);
            if (count == testCtx.getExpectedInvocationCount()) {
                testCtx.getActual().set(sum);
                testCtx.getInvocationCount().addAndGet(count);
                sum = 0;
                count = 0;
                //只会执行一次，使主线程继续原有逻辑
                testCtx.getLatch().countDown();
            }
        }
        return true;
    }

    @Override
    public void destroy() {

    }

    public static class TestRootActorCreator implements TbActorCreator {

        private final TbActorId actorId;
        private final ActorTestCtx testCtx;

        public TestRootActorCreator(TbActorId actorId, ActorTestCtx testCtx) {
            this.actorId = actorId;
            this.testCtx = testCtx;
        }

        @Override
        public TbActorId createActorId() {
            return actorId;
        }

        @Override
        public TbActor createActor() {
            return new TestRootActor(actorId, testCtx);
        }
    }
}
