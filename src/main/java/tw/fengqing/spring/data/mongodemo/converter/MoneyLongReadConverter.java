package tw.fengqing.spring.data.mongodemo.converter;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class MoneyLongReadConverter implements Converter<Long, Money> {
    @Override
    public Money convert(Long source) {
        // 將 Long 值轉換為台幣 Money 對象（假設存儲的是以分為單位）
        return Money.ofMinor(CurrencyUnit.of("TWD"), source);
    }
} 