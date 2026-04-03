package lsieun.knit.report;

import lsieun.knit.event.KnitEvent;

import java.util.List;

public class MarkdownEventReporter implements KnitReporter
{
    @Override
    public String format(List<KnitEvent> events)
    {
        StringBuilder sb = new StringBuilder("| 指令 | 逻辑位置 | 动作 | 针数变化 | 行程 |\n| :--- | :--- | :--- | :--- | :--- |\n");
        for (KnitEvent e : events) {
            String action = e.isPhysical() ? "KNIT" : "ADJUST";
            String passInfo = e.pass().map(p -> p.passType().toString()).orElse("-");
            sb.append(String.format("| %s | R%d-P%d | %s | %d -> %d | %s |\n",
                    e.instruction().getName(), e.binding().repeatIndex(), e.binding().passIndexInRepeat(),
                    action, e.stitchesBefore(), e.stitchesAfter(), passInfo));
        }
        return sb.toString();
    }
}