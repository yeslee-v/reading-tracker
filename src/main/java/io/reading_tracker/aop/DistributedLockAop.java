package io.reading_tracker.aop;

import io.reading_tracker.annotation.DistributedLock;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class DistributedLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";
  private final RedissonClient redissonClient;

  @Around("@annotation(io.reading_tracker.annotation.DistributedLock)")
  public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

    String key =
        REDISSON_LOCK_PREFIX
            + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

    RLock rLock = redissonClient.getLock(key);

    try {
      boolean available =
          rLock.tryLock(
              distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUtil());

      if (!available) {
        log.warn("락 획득 실패 - key: {}", key);
        throw new IllegalArgumentException("현재 처리 중인 요청으로 잠시 후 다시 시도하세요");
      }

      return joinPoint.proceed();
    } catch (InterruptedException e) {
      throw new InterruptedException();
    } finally {
      try {
        if (rLock.isHeldByCurrentThread()) {
          rLock.unlock();
        }
      } catch (IllegalMonitorStateException e) {
        log.info("이미 Redisson 락은 해제되었습니다");
      }
    }
  }
}

class CustomSpringELParser {
  public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
    ExpressionParser parser = new SpelExpressionParser();
    EvaluationContext context = new StandardEvaluationContext();

    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }

    return parser.parseExpression(key).getValue(context, Object.class);
  }
}
