package lsieun.knit.util;

import lsieun.knit.engine.KnitEngine;
import lsieun.knit.event.KnitEvent;
import lsieun.knit.sample.KnitSample;
import org.junit.jupiter.api.Test;

import java.util.List;

class KnitEventPrinterTest
{
    @Test
    void testPrint() {
        KnitEngine engine = KnitSample.getEngine();
        List<KnitEvent> eventLog = engine.getEventLog();
        KnitEventPrinter.print(eventLog);
    }
}