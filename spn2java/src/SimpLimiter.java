import org.andrewkilpatrick.elmGen.ElmProgram;
// ;simple limiter, -12dB threshold, peak detecting
// equ	insig	reg0
// equ	pkfil	reg1
// equ	gain	reg2
// 
// rdax	adcl,0.5
// rdax	adcr,0.5
// wrax	insig,1		;input sum register
// maxx	pkfil,0.99998	;compare with pkfil*.999 (abs)
// wrax	pkfil,1		;write peak value back
// log	-1,-0.125
// exp	1,0		;1/x
// wrax	gain,1
// mulx	adcl
// sof	-2,0
// sof	-1.5,0		;restore gain, to avoid output clipping
// wrax	dacl,0
// ldax	gain
// mulx	adcr
// sof	-2,0
// sof	-1.5,0
// wrax	dacr,0		;write outputs, zero ACC
public class SimpLimiter extends ElmProgram {
  public SimpLimiter() {
    super("SimpLimiter");
    setSamplerate(48000);
    int insig = REG0;
    int pkfil = REG1;
    int gain = REG2;
    readRegister(ADCL, 0.5);
    readRegister(ADCR, 0.5);
    writeRegister(insig, 1);
    maxx(pkfil, 0.99998);
    writeRegister(pkfil, 1);
    log(-1, -0.125);
    exp(1, 0);
    writeRegister(gain, 1);
    mulx(ADCL);
    scaleOffset(-2, 0);
    scaleOffset(-1.5, 0);
    writeRegister(DACL, 0);
    loadAccumulator(gain);
    mulx(ADCR);
    scaleOffset(-2, 0);
    scaleOffset(-1.5, 0);
    writeRegister(DACR, 0);
  }
}
