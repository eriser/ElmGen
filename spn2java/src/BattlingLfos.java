import org.andrewkilpatrick.elmGen.ElmProgram;
// ;Battling LFOs
// ;multiple interactive sin generators
// ;just let it run!
// 
// equ	s1	reg0
// equ	c1	reg1
// equ	s2	reg2
// equ	c2	reg3
// equ	s3	reg4
// equ	c3	reg5
// equ	s4	reg6
// equ	c4	reg7
// 
// equ	k1	reg8
// equ	k2	reg9
// equ	k3	reg10
// 
// ;prepare pots for defining weapons and the battle zone:
// 
// rdax	pot0,1
// sof	0.01,0.002
// wrax	k1,0
// 
// rdax	pot1,1
// sof	0.03,0.01
// wrax	k2,0
// 
// rdax	pot2,1
// sof	0.2,0.05
// wrax	k3,0
// 
// ;charge weapons for all three players:
// 
// skp	run,5
// sof	0,0.5
// wrax	c1,1
// wrax	c2,1
// wrax	c3,1
// wrax	c4,0
// 
// ;do oscillators:
// 
// rdax	c1,1
// mulx	k1
// mulx	c4
// rdax	s1,1
// wrax	s1,-1
// mulx	k1
// mulx	c4
// rdax	c1,1
// wrax	c1,0
// 
// rdax	c2,1
// mulx	k2
// mulx	c1
// rdax	s2,1
// wrax	s2,-1
// mulx	k2
// mulx	c1
// rdax	c2,1
// wrax	c2,0
// 
// rdax	c3,1
// mulx	k3
// mulx	c2
// rdax	s3,1
// wrax	s3,-1
// mulx	k3
// mulx	c2
// rdax	c3,1
// wrax	c3,0
// 
// rdax	c4,0.5		;newbie, no weapon control.
// mulx	c3
// rdax	s4,1
// wrax	s4,-0.5
// mulx	c3
// rdax	c4,1
// wrax	c4,0
// 
// ;sum outputs, lowest freq down middle:
// 
// rdax	c1,0.4
// rdax	c2,0.2
// rdax	s3,0.2
// rdax	c4,0.2
// wrax	dacl,0
// 
// rdax	c1,0.4
// rdax	c2,0.2
// rdax	c3,0.2
// rdax	s4,0.2
// wrax	dacr,0
// 
// ;KB 7/12/06
public class BattlingLfos extends ElmProgram {
  public BattlingLfos() {
    super("BattlingLfos");
    setSamplerate(48000);
    int s1 = REG0;
    int c1 = REG1;
    int s2 = REG2;
    int c2 = REG3;
    int s3 = REG4;
    int c3 = REG5;
    int s4 = REG6;
    int c4 = REG7;
    int k1 = REG8;
    int k2 = REG9;
    int k3 = REG10;
    readRegister(POT0, 1);
    scaleOffset(0.01, 0.002);
    writeRegister(k1, 0);
    readRegister(POT1, 1);
    scaleOffset(0.03, 0.01);
    writeRegister(k2, 0);
    readRegister(POT2, 1);
    scaleOffset(0.2, 0.05);
    writeRegister(k3, 0);
    skip(SKP_RUN, 5);
    scaleOffset(0, 0.5);
    writeRegister(c1, 1);
    writeRegister(c2, 1);
    writeRegister(c3, 1);
    writeRegister(c4, 0);
    readRegister(c1, 1);
    mulx(k1);
    mulx(c4);
    readRegister(s1, 1);
    writeRegister(s1, -1);
    mulx(k1);
    mulx(c4);
    readRegister(c1, 1);
    writeRegister(c1, 0);
    readRegister(c2, 1);
    mulx(k2);
    mulx(c1);
    readRegister(s2, 1);
    writeRegister(s2, -1);
    mulx(k2);
    mulx(c1);
    readRegister(c2, 1);
    writeRegister(c2, 0);
    readRegister(c3, 1);
    mulx(k3);
    mulx(c2);
    readRegister(s3, 1);
    writeRegister(s3, -1);
    mulx(k3);
    mulx(c2);
    readRegister(c3, 1);
    writeRegister(c3, 0);
    readRegister(c4, 0.5);
    mulx(c3);
    readRegister(s4, 1);
    writeRegister(s4, -0.5);
    mulx(c3);
    readRegister(c4, 1);
    writeRegister(c4, 0);
    readRegister(c1, 0.4);
    readRegister(c2, 0.2);
    readRegister(s3, 0.2);
    readRegister(c4, 0.2);
    writeRegister(DACL, 0);
    readRegister(c1, 0.4);
    readRegister(c2, 0.2);
    readRegister(c3, 0.2);
    readRegister(s4, 0.2);
    writeRegister(DACR, 0);
  }
}
