import org.andrewkilpatrick.elmGen.ElmProgram;
// ;rms limiter, approx 10dB limiting range.
// ;stereo I/O, common control (to maintain image)
// 
// equ	sigin	reg0
// equ	avg	reg1
// equ	gain	reg2
// 
// rdax	adcl,0.5
// rdax	adcr,0.5
// wrax	sigin,1		;write mono input signal
// mulx	sigin		;square input
// rdfx	avg,0.001	;average squared result
// wrax	avg,1		;avg stored and in ACC
// log	-0.5,-0.125	;square root and 1/x combined
// exp	1,0		;
// wrax	gain,1
// mulx	adcl
// sof	1.5,0		;restore gain, but avoid output clipping
// wrax	dacl,0
// rdax	gain,1
// mulx	adcr
// sof	1.5,0
// wrax	dacr,0		;write outputs, zero ACC
public class RMSLimiter extends ElmProgram {
  public RMSLimiter() {
    super("RMSLimiter");
    setSamplerate(48000);
    int sigin = REG0;
    int avg = REG1;
    int gain = REG2;
    readRegister(ADCL, 0.5);
    readRegister(ADCR, 0.5);
    writeRegister(sigin, 1);
    mulx(sigin);
    readRegisterFilter(avg, 0.001);
    writeRegister(avg, 1);
    log(-0.5, -0.125);
    exp(1, 0);
    writeRegister(gain, 1);
    mulx(ADCL);
    scaleOffset(1.5, 0);
    writeRegister(DACL, 0);
    readRegister(gain, 1);
    mulx(ADCR);
    scaleOffset(1.5, 0);
    writeRegister(DACR, 0);
  }
}
