/*
Copyright 2014 Red Hat, Inc. and/or its affiliates.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */


import com.clearspring.analytics.stream.cardinality.HyperLogLog;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the SiteSession class
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), changed by ZequnLi
 * @since 2014-01
 */
public class SiteSession {

    public static long MAX_IDLE_MS = TimeUnit.MINUTES.toMillis(30);
    private static long globalLastHitMillis;

    private final int id;
    private final long firstHitMillis;
    private long lastHitMillis;
    private long hitCount = 0;

    private final HyperLogLog hyperLogLog = new HyperLogLog(0.05);


    /**
     * Creates a new SiteSession instance based on its first hit.
     *
     * @param id the session id
     * @param firstHitMillis the time of the first hit in the session, in milliseconds since unix epoch
     * @param url the url of the first hit
     */
    public SiteSession(int id, long firstHitMillis, String url) {

        this.id = id;
        this.firstHitMillis = firstHitMillis;
        update(firstHitMillis, url);
    }

    public int getId() {
        return id;
    }

    public long getFirstHitMillis() {
        return firstHitMillis;
    }

    public long getLastHitMillis() {
        return lastHitMillis;
    }

    public long getHitCount() {
        return hitCount;
    }

    public HyperLogLog getHyperLogLog() {

        return hyperLogLog;
    }

    /**
     * Modify the session by adding a new hit.
     *
     * @param hitMillis the time of the hit in the session, in milliseconds since unix epoch
     * @param url the url of the hit
     *
     * @throws java.lang.IllegalArgumentException if the time is less that the global max
     * or after the session's timeout
     */
    public SiteSession update(long hitMillis, String url) {

        if(lastHitMillis > 0 && lastHitMillis+MAX_IDLE_MS < hitMillis) {
            //SiteSession.resetGlobalMax();
            return new SiteSession(this.id,hitMillis,url);
        }
        this.lastHitMillis = hitMillis;

        if(hitMillis < globalLastHitMillis) {
            throw new IllegalArgumentException("hit processed out of order");
        }
        globalLastHitMillis = hitMillis;

        hitCount++;
        hyperLogLog.offer(url);
        return null;
    }

    /**
     * Returns true if the global last hit (i.e. virtual clock) has advanced such that
     * any in-order update to this session would now exceed its timeout threshold
     *
     * @return true if the session has expired, false otherwise
     */
    public boolean isExpired() {
        return globalLastHitMillis-lastHitMillis > MAX_IDLE_MS;
    }

    /**
     * Reset the global session 'clock'. Intended only for test use
     */
    public static void resetGlobalMax() {
        globalLastHitMillis = 0;
    }

    @Override
    public String toString() {
        return "SiteSession{" +
                "firstHit=" + new Date(firstHitMillis) +
                ", hitCount=" + hitCount +
                ", id='" + id + '\'' +
                ", lastHit=" + new Date(lastHitMillis) +
                ",urlCounter=" + hyperLogLog.cardinality()+
                '}';
    }
}