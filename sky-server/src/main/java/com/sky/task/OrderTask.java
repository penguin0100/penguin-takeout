package com.sky.task;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.utils.RedisLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RedisLockUtil redisLockUtil;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ? ") //每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        // select * from orders where status = ? and order_time < (当前时间 - 15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        if(ordersList != null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
                // 恢复 Redis 库存
                restoreStock(orders.getId());
            }
        }
    }

    /**
     * 恢复订单占用的 Redis 库存
     */
    private void restoreStock(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
        for (OrderDetail detail : orderDetails) {
            if (detail.getDishId() != null) {
                String stockKey = RedisLockUtil.getDishStockKey(detail.getDishId());
                String lockKey = RedisLockUtil.getDishLockKey(detail.getDishId());
                try {
                    redisLockUtil.tryLock(lockKey, 10);
                    redisTemplate.opsForValue().increment(stockKey, detail.getNumber());
                } finally {
                    redisLockUtil.unlock(lockKey);
                }
            }
            if (detail.getSetmealId() != null) {
                String stockKey = RedisLockUtil.getSetmealStockKey(detail.getSetmealId());
                String lockKey = RedisLockUtil.getSetmealLockKey(detail.getSetmealId());
                try {
                    redisLockUtil.tryLock(lockKey, 10);
                    redisTemplate.opsForValue().increment(stockKey, detail.getNumber());
                } finally {
                    redisLockUtil.unlock(lockKey);
                }
            }
        }
    }

    //处理一直处于派送中状态的订单
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点触发一次
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单：{}",LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if(ordersList != null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
