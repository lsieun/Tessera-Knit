package lsieun.knit.event;

import java.util.List;

/**
 * 编织事件流转换器接口
 *
 * @param <T> 转换后的目标类型
 */
public interface KnitEventConverter<T>
{
    T convert(List<KnitEvent> events);
}