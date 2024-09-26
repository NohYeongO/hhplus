package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    @Autowired
    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    public UserPoint selectPoint(long id) {
        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectHistoryList(long id) {
        List<PointHistory> historyList = pointHistoryTable.selectAllByUserId(id);
        if (historyList.isEmpty()) throw new IllegalArgumentException("충전/이용 내역이 없음");
        return historyList;
    }

    public UserPoint pointCharge(long id, long amount) {
        long newPoint = selectPoint(id).point() + amount;

        if(newPoint > PointController.MAX_LIMIT) throw new IllegalArgumentException("최대 잔고를 초과");

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPointTable.insertOrUpdate(id, newPoint);
    }

    public UserPoint pointUse(long id, long amount) {
        long newPoint = selectPoint(id).point() - amount;
        if(newPoint < 0) throw new IllegalArgumentException("잔고 부족");

        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
        return userPointTable.insertOrUpdate(id, newPoint);
    }

}
