package lsieun.knit.util;

import lsieun.knit.engine.KnitEngine;
import lsieun.knit.model.KnitFabric;
import lsieun.knit.sample.KnitSample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnitFabricHelperTest
{

    @Test
    void printVisualShape()
    {
        KnitEngine engine = KnitSample.getEngine();
        KnitFabric fabric = KnitFabricHelper.getFabric(engine, "Xxx");
        KnitFabricHelper.printVisualShape(fabric);
    }

    @Test
    void testSaveSvg() {
        KnitEngine engine = KnitSample.getEngine();
        KnitFabric fabric = KnitFabricHelper.getFabric(engine, "Xxx");
        KnitFabricHelper.saveAsSvg(fabric, "D:\\git-repo\\Tessera-Knit\\target\\fabric-preview.svg");
    }
}