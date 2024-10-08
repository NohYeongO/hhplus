## 동시성 제어를 위한 ReentrantLock 활용

`new ReentrantLock(true)`를 활용하여 동시성 제어를 구현했습니다.  
ReentrantLock은 두 가지 방식으로 사용할 수 있다는 사실을 알게 되었고,  
`true` 값을 주입하면 공정성을 보장하여 순서를 지키도록 동작한다는 것을 배웠습니다.

## ReentrantLock 공정성과 비공정성 비교

### 1. **비공정 모드** (Non-Fair Mode)
- 스레드가 **락을 빨리 획득**할 수 있어 **성능이 좋을 수 있다**.
- 하지만 특정 스레드가 계속해서 락을 획득하지 못하는 **기아 상태**(Starvation)가 발생할 수 있다.
- **순서 보장 X**.

### 2. **공정 모드** (Fair Mode)
- **큐 형태**로 요청을 저장하여 스레드가 락을 획득하는 **순서를 보장**.
- 하지만 **성능이 떨어질 수 있는 단점**이 있다.
- **순서 보장 O**.

---

## 코드

```java
lock.lock();
try{
    long newPoint = selectPoint(id).point() + amount;
    if(newPoint > PointController.MAX_LIMIT) throw new IllegalArgumentException("최대 잔고를 초과");

    pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
    return userPointTable.insertOrUpdate(id, newPoint);
}finally {
    lock.unlock();
}

// 통합 테스트 코드
@DisplayName("동시성 테스트 / 통합테스트")
    @Test
    void chargeOrUse(){
        long userId = 1L;

        // 여러 개의 비동기 작업을 동시에 실행
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->{
                    pointController.charge(userId, 5000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.charge(userId, 4000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.use(userId, 3000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.charge(userId, 5000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.use(userId, 4000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.use(userId, 3000);
                })
        ).join();

        // history에서 조회
        List<PointHistory> list = pointController.history(userId).getBody();
        long[] point = {5000, 4000, 3000, 5000, 4000, 3000};

        // 순서 비교
        for (int i = 0; i < list.size(); i++) {
            long actualPoint = list.get(i).amount();
            assertEquals(point[i], actualPoint);
        }
        // userId로 조회
        UserPoint userPoint = pointController.point(userId).getBody();

        // 연산 값 확인
        assertEquals(userPoint.point(), 5000 + 4000 - 3000 + 5000 - 4000 - 3000);
```
`try-finally` 구문을 사용해, 락을 해제하는 방법을 적용했습니다.  
스레드가 요청을 끝내면 **다른 스레드가** 락을 획득해서 **활용할 수 있도록** 설정했습니다.

## 고민 및 개선 사항
- 순서를 보장하더라도 중간에 락을 획득한 스레드에 문제가 발생하면 **해결 방안이 필요**.
- 사용자가 다를 경우 **사용자별로 별도의 처리가 필요할지 고민**.
- 동시성 처리에 대해 **추가적인 공부가 필요함을 느끼고 있음**.
- 앞으로 **동시성 제어와 해결책을 더 알아볼 계획**입니다.
