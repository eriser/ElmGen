import org.andrewkilpatrick.elmGen.ElmProgram;
// ;program for gain and loss (pot0)
// ;+/- 20dB
// ;adc to dac
// 
// equ	pfil	reg0
// equ	pexp	reg1
// 
// or	$800000			;put -1 in acc
// rdax	pot0,1			;pot out = -1 to 0
// rdfx	pfil,0.01
// wrax	pfil,1			;filter pot
// sof	0.41666,0		;-40dB to 0 dB
// exp	1,0
// wrax	pexp,0
// 
// rdax	adcl,1
// mulx	pexp
// sof	-2,0
// sof	-2,0
// sof	-2,0
// sof	-1.25,0
// wrax	dacl,0
// 
// rdax	adcr,1
// mulx	pexp
// sof	-2,0
// sof	-2,0
// sof	-2,0
// sof	-1.25,0
// wrax	dacr,0
public class ThruGain extends ElmProgram {
  public ThruGain() {
    super("ThruGain");
    setSamplerate(48000);
    int pfil = REG0;
    int pexp = REG1;
    or(0x800000);
    readRegister(POT0, 1);
    readRegisterFilter(pfil, 0.01);
    writeRegister(pfil, 1);
    scaleOffset(0.41666, 0);
    exp(1, 0);
    writeRegister(pexp, 0);
    readRegister(ADCL, 1);
    mulx(pexp);
    scaleOffset(-2, 0);
    scaleOffset(-2, 0);
    scaleOffset(-2, 0);
    scaleOffset(-1.25, 0);
    writeRegister(DACL, 0);
    readRegister(ADCR, 1);
    mulx(pexp);
    scaleOffset(-2, 0);
    scaleOffset(-2, 0);
    scaleOffset(-2, 0);
    scaleOffset(-1.25, 0);
    writeRegister(DACR, 0);
  }
}
