package lsieun.knit.sample;

import lsieun.knit.engine.KnitEngine;
import lsieun.knit.instruction.KnitInstruction;
import lsieun.knit.util.InstructionParser;

import java.util.List;

public class KnitSample
{
    public static KnitEngine getEngine()
    {
        // 6转、6+1+30、7+1+10、19转、20针（平收）、1转、1-2-2、1.5-2-8、2-2-24、1.5-2-6、1-2-4
        String[] instructionArray = {
                "1-0-1",
                "1-2-6",
                "1-2-4",
                "1.5-2-6",
                "2-2-24",
                "1.5-2-8",
                "1-2-2",
                "1转",
                "-20针",
                "19转",
                "7+1+10",
                "6+1+30",
                "6转"
        };


        InstructionParser parser = new InstructionParser();
        List<KnitInstruction> techSheet = parser.parse(instructionArray);

        KnitEngine engine = new KnitEngine(267);
        engine.processAll(techSheet);
        return engine;
    }
}
