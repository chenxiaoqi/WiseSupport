package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.Court;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

class BookScheduler {

    private Map<Date, ScheduleCourts> map = new HashMap<>();

    private long createTime;

    private BookSchedulerRepository repository;

    private int arenaId;

    BookScheduler(Arena arena, BookingMapper mapper, BookSchedulerRepository repository) {
        this.arenaId = arena.getId();
        this.repository = repository;
        this.createTime = System.currentTimeMillis();

        Date start = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        for (int i = 0; i < arena.getAdvanceBookDays(); i++) {
            map.put(DateUtils.addDays(start, i), new ScheduleCourts(arena.getCourts()));
        }
        Date end = DateUtils.addDays(start, arena.getAdvanceBookDays() - 1);
        List<Booking> bookings = mapper.queryInRange(arena.getId(), start, end);
        for (Booking booking : bookings) {
            ScheduleCourts courts = map.get(booking.getDate());
            ScheduleCourt court = courts.getScheduleCourt(booking.getCourt().getId());
            court.init(booking.getStart(), booking.getEnd());
        }
    }

    synchronized boolean isValid() {
        Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        return createTime >= today.getTime();
    }

    synchronized void book(Date date, int courtId, int begin, int end) {
        map.get(date).getScheduleCourt(courtId).book(begin, end);
    }

    synchronized void release(Date date, int courtId, int begin, int end) {
        map.get(date).getScheduleCourt(courtId).release(begin, end);
    }

    synchronized void invalidate() {
        repository.invalidate(arenaId);
    }

    List<BookingSimple> getBookings(Date date) {
        List<BookingSimple> bookings = new ArrayList<>();
        for (ScheduleCourt court : map.get(date).getAll()) {
            court.appendBookings(date, bookings);
        }
        return bookings;
    }

    private static class ScheduleCourt {
        int courtId;
        boolean[] periods = new boolean[48];

        ScheduleCourt(int courtId) {
            this.courtId = courtId;
        }

        void book(int begin, int end) {
            for (int i = begin; i <= end; i++) {
                if (periods[i]) {
                    throw new BusinessException("对不起,该时间段已被预定");
                }
            }
            for (int i = begin; i <= end; i++) {
                periods[i] = true;
            }
        }

        void init(Integer start, Integer end) {
            for (int i = start; i <= end; i++) {
                periods[i] = true;
            }
        }

        public void release(int begin, int end) {
            for (int i = begin; i <= end; i++) {
                periods[i] = false;
            }
        }

        void appendBookings(Date date, List<BookingSimple> bookings) {
            BookingSimple booking = null;
            boolean expectBegin = true;
            for (int i = 0; i < periods.length; i++) {
                if (periods[i]) {
                    if (expectBegin) {
                        booking = new BookingSimple(date, this.courtId, i);
                        expectBegin = false;
                    }
                } else {
                    if (!expectBegin) {
                        booking.setEnd(i - 1);
                        expectBegin = true;
                        bookings.add(booking);
                    }
                }
            }
            if (!expectBegin) {
                booking.setEnd(periods.length - 1);
                bookings.add(booking);
            }
        }
    }

    private static class ScheduleCourts {
        HashMap<Integer, ScheduleCourt> map = new HashMap<>();

        ScheduleCourts(List<Court> courts) {
            for (Court court : courts) {
                map.put(court.getId(), new ScheduleCourt(court.getId()));
            }
        }

        ScheduleCourt getScheduleCourt(int courtId) {
            return Objects.requireNonNull(map.get(courtId));
        }

        Collection<ScheduleCourt> getAll() {
            return map.values();
        }
    }
}
