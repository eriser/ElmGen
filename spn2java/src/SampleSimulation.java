import java.io.IOException;
import org.andrewkilpatrick.elmGen.simulator.SpinSimulator;
import org.andrewkilpatrick.elmGen.ElmProgram;

public class SampleSimulation {
  public SampleSimulation(String args[]) {
    String className = "ThruGain";
    String inputFile = "test_in.wav";
    String outputFile = null;
    if (args.length >= 1) {
      className = args[0];
      if (args.length >= 2) {
        inputFile = args[1];
        if (args.length >= 3) {
          outputFile = args[2];
        }
      }
    }
    ElmProgram p = null;
    try {
      p = (ElmProgram)Class.forName(className).newInstance();
    } catch (Exception e) {
      System.out.println(e);
      p = new ThruGain();
    }
    SpinSimulator sim = new SpinSimulator(
        p,
        inputFile,
        outputFile, 0.5, 0.5, 0.5);
    sim.showInteractiveControls();
    sim.showLevelLogger();
    sim.setLoopMode(false);
    sim.run();
    System.exit(0);
  }
  
  public static void main(String args[]) {
     new SampleSimulation(args);    
  }
}
