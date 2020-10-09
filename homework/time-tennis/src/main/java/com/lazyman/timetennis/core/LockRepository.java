package com.lazyman.timetennis.core;

import com.wisesupport.commons.exceptions.BusinessException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LockRepository {

    private static final ConcurrentHashMap<Integer, Lock> locks = new ConcurrentHashMap<>();

    public Lock require(int arenaId) {
        Lock lock = locks.computeIfAbsent(arenaId, integer -> new Lock(new ReentrantLock()));
        if (!lock.lock()) {
            throw new BusinessException("该场馆预定忙,请稍后重试");
        }
        return lock;
    }

    public static class Lock {

        private ReentrantLock reentrantLock;

        Lock(ReentrantLock reentrantLock) {
            this.reentrantLock = reentrantLock;
        }

        boolean lock() {
            return this.reentrantLock.tryLock();
        }

        public void unlock() {
            this.reentrantLock.unlock();
        }
    }
}
