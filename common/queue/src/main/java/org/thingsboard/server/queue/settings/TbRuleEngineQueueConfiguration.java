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
package org.thingsboard.server.queue.settings;

import lombok.Data;

@Data
public class TbRuleEngineQueueConfiguration {

    /**
     * thingsboard.yml
     *
     *  rule-engine:
     *     queues:
     *          name: "${TB_QUEUE_RE_MAIN_QUEUE_NAME:Main}"
     *         topic: "${TB_QUEUE_RE_MAIN_TOPIC:tb_rule_engine.main}"
     *         poll-interval: "${TB_QUEUE_RE_MAIN_POLL_INTERVAL_MS:25}"
     *         partitions: "${TB_QUEUE_RE_MAIN_PARTITIONS:10}"
     *         pack-processing-timeout: "${TB_QUEUE_RE_MAIN_PACK_PROCESSING_TIMEOUT_MS:2000}"
     *         submit-strategy:
     *           type: "${TB_QUEUE_RE_MAIN_SUBMIT_STRATEGY_TYPE:BURST}" # BURST, BATCH, SEQUENTIAL_BY_ORIGINATOR, SEQUENTIAL_BY_TENANT, SEQUENTIAL
     *           # For BATCH only
     *           batch-size: "${TB_QUEUE_RE_MAIN_SUBMIT_STRATEGY_BATCH_SIZE:1000}" # Maximum number of messages in batch
     *         processing-strategy:
     *           type: "${TB_QUEUE_RE_MAIN_PROCESSING_STRATEGY_TYPE:SKIP_ALL_FAILURES}" # SKIP_ALL_FAILURES, RETRY_ALL, RETRY_FAILED, RETRY_TIMED_OUT, RETRY_FAILED_AND_TIMED_OUT
     *           # For RETRY_ALL, RETRY_FAILED, RETRY_TIMED_OUT, RETRY_FAILED_AND_TIMED_OUT
     *           retries: "${TB_QUEUE_RE_MAIN_PROCESSING_STRATEGY_RETRIES:3}" # Number of retries, 0 is unlimited
     *           failure-percentage: "${TB_QUEUE_RE_MAIN_PROCESSING_STRATEGY_FAILURE_PERCENTAGE:0}" # Skip retry if failures or timeouts are less then X percentage of messages;
     *           pause-between-retries: "${TB_QUEUE_RE_MAIN_PROCESSING_STRATEGY_RETRY_PAUSE:3}"# Time in seconds to wait in consumer thread before retries;
     *           max-pause-between-retries: "${TB_QUEUE_RE_MAIN_PROCESSING_STRATEGY_MAX_RETRY_PAUSE:3}"# Max allowed time in seconds for pause between retries.
     *
     */
    private String name;
    private String topic;
    private int pollInterval;
    private int partitions;
    private long packProcessingTimeout;
    private TbRuleEngineQueueSubmitStrategyConfiguration submitStrategy;
    private TbRuleEngineQueueAckStrategyConfiguration processingStrategy;

}
