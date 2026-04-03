package lsieun.knit.report;

import lsieun.knit.event.KnitEvent;

import java.util.List;

public interface KnitReporter
{
    // 传入仓库数据，输出格式化字符串
    String format(List<KnitEvent> events);
}