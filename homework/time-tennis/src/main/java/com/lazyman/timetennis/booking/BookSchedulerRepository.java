package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class BookSchedulerRepository implements ApplicationListener<BookingCancelEvent> {
    private Map<Integer, BookScheduler> cache = new ConcurrentHashMap<>();

    private BookingMapper mapper;

    private ArenaDao arenaDao;


    public BookSchedulerRepository(BookingMapper mapper, ArenaDao arenaDao) {
        this.mapper = mapper;
        this.arenaDao = arenaDao;
    }

    BookScheduler arenaScheduler(Integer arenaId) {
        return arenaScheduler(arenaDao.loadFull(arenaId));
    }

    synchronized BookScheduler arenaScheduler(Arena arena) {
        BookScheduler bs = cache.get(arena.getId());
        if (bs != null && bs.isValid()) {
            return bs;
        }
        log.info("create book scheduler for arena {}", arena.getId());
        bs = new BookScheduler(arena, mapper, this);
        cache.put(arena.getId(), bs);
        return bs;
    }

    void invalidate(int arenaId) {
        cache.remove(arenaId);
    }

    @Override
    public void onApplicationEvent(BookingCancelEvent event) {
        Booking booking = event.getBooking();
        BookScheduler scheduler = cache.get(booking.getArena().getId());
        if (scheduler == null) {
            log.info("no scheduler found for arena {}", booking.getArena().getId());
            return;
        }
        scheduler.release(booking.getDate(), booking.getCourt().getId(), booking.getStart(), booking.getEnd());
    }
}
