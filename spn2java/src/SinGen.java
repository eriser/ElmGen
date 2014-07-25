import org.andrewkilpatrick.elmGen.ElmProgram;
// ;sine wave generator
// 
// equ	freq	reg0
// equ	s	reg1
// equ	c	reg2
// equ	p0fil	reg3	;coarse frequency control filter
// equ	p2fil	reg4	;output level control filter
// equ	amp	reg5	;output amplitude
// 
// skp	run,2
// SOF	0,0.5		;setup LFO with amplitude of 0.5
// wrax	s,0		;c is initialized to zero on startup
// 
// rdax	pot1,1		;get fine freq control
// sof	0.01,-0.005	;scale to proper exponential limits
// rdax	pot0,0.625	;add coarse freq control
// sof	1,-0.66
// exp	1,0
// rdfx	p0fil,0.01	;average with filter
// wrax	p0fil,1
// wrax	freq,0
// 
// rdax	pot2,15/16	;set up pot2 for db/step amplitude control
// rdfx	p2fil,0.01
// wrax	p2fil,1
// sof	1,-15/16
// exp	1,0
// wrax	amp,0
// 
// rdax	c,1		;oversample the oscillator to reach 20KHz easily.
// mulx	freq
// rdax	s,1
// wrax	s,-1
// mulx	freq
// rdax	c,1
// wrax	c,0
// 
// rdax	c,1
// mulx	freq
// rdax	s,1
// wrax	s,-1
// mulx	freq
// rdax	c,1
// wrax	c,0
// 
// rdax	c,1
// mulx	freq
// rdax	s,1
// wrax	s,-1
// mulx	freq
// rdax	c,1
// wrax	c,0
// 
// rdax	c,1
// mulx	freq
// rdax	s,1
// wrax	s,-1
// mulx	freq
// rdax	c,1
// wrax	c,1.99
// 
// mulx	amp		;scale output
// 
// wrax	dacl,1
// wrax	dacr,0		;write both outputs
// 
public class SinGen extends ElmProgram {
  public SinGen() {
    super("SinGen");
    setSamplerate(48000);
    int freq = REG0;
    int s = REG1;
    int c = REG2;
    int p0fil = REG3;
    int p2fil = REG4;
    int amp = REG5;
    skip(SKP_RUN, 2);
    scaleOffset(0, 0.5);
    writeRegister(s, 0);
    readRegister(POT1, 1);
    scaleOffset(0.01, -0.005);
    readRegister(POT0, 0.625);
    scaleOffset(1, -0.66);
    exp(1, 0);
    readRegisterFilter(p0fil, 0.01);
    writeRegister(p0fil, 1);
    writeRegister(freq, 0);
    readRegister(POT2, 15/16);
    readRegisterFilter(p2fil, 0.01);
    writeRegister(p2fil, 1);
    scaleOffset(1, -15/16);
    exp(1, 0);
    writeRegister(amp, 0);
    readRegister(c, 1);
    mulx(freq);
    readRegister(s, 1);
    writeRegister(s, -1);
    mulx(freq);
    readRegister(c, 1);
    writeRegister(c, 0);
    readRegister(c, 1);
    mulx(freq);
    readRegister(s, 1);
    writeRegister(s, -1);
    mulx(freq);
    readRegister(c, 1);
    writeRegister(c, 0);
    readRegister(c, 1);
    mulx(freq);
    readRegister(s, 1);
    writeRegister(s, -1);
    mulx(freq);
    readRegister(c, 1);
    writeRegister(c, 0);
    readRegister(c, 1);
    mulx(freq);
    readRegister(s, 1);
    writeRegister(s, -1);
    mulx(freq);
    readRegister(c, 1);
    writeRegister(c, 1.99);
    mulx(amp);
    writeRegister(DACL, 1);
    writeRegister(DACR, 0);
  }
}
