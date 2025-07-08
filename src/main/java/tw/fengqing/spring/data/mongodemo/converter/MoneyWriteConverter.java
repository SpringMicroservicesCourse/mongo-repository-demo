package tw.fengqing.spring.data.mongodemo.converter;

import org.joda.money.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class MoneyWriteConverter implements Converter<Money, Long> {
    @Override
    public Long convert(Money source) {
        // 將 Money 轉換為以分為單位的 Long 值存儲
        return source.getAmountMinorLong();
    }
} 