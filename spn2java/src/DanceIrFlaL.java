import org.andrewkilpatrick.elmGen.ElmProgram;
// ;disco mixer program
// ;pot0 = reverb to infinity
// ;pot1 = flange; zero delay at full counter clockwise
// ;pot2 = low pass filter (4 pole)
// 
// equ	krt	reg0
// equ	kin	reg1
// equ	kmix	reg2
// equ	lp1al	reg3
// equ	lp1bl	reg4
// equ	lp1ar	reg5
// equ	lp1br	reg6
// equ	lp2al	reg7
// equ	lp2bl	reg8
// equ	lp2ar	reg9
// equ	lp2br	reg10
// equ	stop	reg11
// equ	pbyp	reg12
// equ	fol	reg13
// equ	forr	reg14
// equ	rol	reg15
// equ	ror	reg16
// equ	kfl	reg17
// equ	temp	reg18
// equ	rmixl	reg19
// equ	rmixr	reg20
// equ	lbyp	reg21
// 
// mem	ap1	202
// mem	ap2	541
// 
// mem	dap1a	2204
// mem	dap1b	2701
// mem	del1	4456
// mem	dap2a	2532
// mem	dap2b	2201
// mem	del2	6325
// 
// mem	fdell	512
// mem	fdelr	512
// 
// equ	kap	0.6
// equ	kql	-0.4
// 
// ;prepare flange delay pointer:
// 
// skp	run,1
// wldr	0,0,512
// 
// ;prepare pots to affect control variables:
// ;pot0 controls reverb time, but also affects input drive level;
// ;reveb time is moderate up to about mid position, then increases
// ;to infinity (or nearly) at full position.
// ;input drive is constant, but decreases at the full pot0 position.
// ;output mix is varied over the first half of pot0, then remains
// ;high to the end of pot0's range.
// 
// rdax	pot0,1.999	;get pot0, clip the upper half of pot0's range.
// wrax	kmix,0		;write the output mix value
// 
// rdax	pot0,-1		;get pot0 again, 0 to -1
// sof	1,0.999		;now +1 to 0
// sof	1.999,0		;now +1 until midpint, then decreases to 0
// wrax	kin,0		;write the input attenuator value
// 
// rdax	pot0,1		;get pot0 again
// wrax	krt,1		;save in krt, keep in ACC
// sof	1,-0.5		;subtract 1/2
// skp	gez,2		;skp if pot is in upper half of range
// sof	0,0.5		;load accumulator with +0.5
// wrax	krt,0		;overwrite if pot is in lower half of range
// 
// 
// ;prepare pot2 for low pass frequency control:
// 
// rdax	pot2,1		;get pot2
// sof	0.35,-0.35	;ranges -0.3 to 0
// exp	1,0
// wrax	kfl,0		;write to LP filter control
// 
// ;now derive filter bypass function (at open condition)
// 
// rdax	pot2,1		;read pot2 (LP) again
// sof	1,-0.999
// exp	1,0
// wrax	lbyp,0
// 
// ;now do reverb, simple, twin loop, mono drive:
// 
// rdax	adcl,0.25
// rdax	adcr,0.25	;get inputs, leave headroom
// mulx	kin		;scale by input attenuator
// rda	ap1#,kap	;4 all passes:
// wrap	ap1,-kap
// rda	ap2#,kap
// wrap	ap2,-kap
// wrax	temp,0		;write ap output to temp reg
// 
// rda	del2#,1		;do reverb loop:
// mulx	krt
// rdax	temp,1
// rda	dap1a#,kap
// wrap	dap1a,-kap
// rda	dap1b#,kap
// wrap	dap1b,-kap
// wra	del1,0
// rda	del1#,1
// mulx	krt
// rdax	temp,1
// rda	dap2a#,kap
// wrap	dap2a,-kap
// rda	dap2b#,kap
// wrap	dap2b,-kap
// wra	del2,0
// 
// ;now mix the inputs with the reverb:
// 
// rdax	adcl,-1
// rda	del1,1
// mulx	pot0
// rdax	adcl,1
// wra	fdell,0		;write temp reverb output
// 
// rdax	adcr,-1
// rda	del2,1
// mulx	pot0
// rdax	adcr,1
// wra	fdelr,0
// 
// ;Reverb outputs are in flange delays.
// ;now do flange:
// 
// cho 	rda,rmp0,reg|compc,fdell
// cho 	rda,rmp0,0,fdell+1
// sof	0.707,0
// rda	fdell,0.707
// wrax	fol,0
// 
// cho 	rda,rmp0,compc,fdelr
// cho 	rda,rmp0,0,fdelr+1
// sof	0.707,0
// rda	fdelr,0.707
// wrax	forr,0
// 
// ;get LFO pointer position and control with pot1:
// 
// cho	rdal,rmp0	;get pointer from ramp 0
// rdax	pot1,-0.03	;add in pot 1
// wrax	rmp0_rate,1	;write difference to ramp so that
// ;pointer will track pot1
// ;now do low pass filter, stereo:
// 
// rdax	lp1al,1
// mulx	kfl
// rdax	lp1bl,1
// wrax	lp1bl,-1
// rdax	lp1al,kql
// rdax	fol,1
// mulx	kfl
// rdax	lp1al,1
// wrax	lp1al,0
// 
// rdax	lp2al,1
// mulx	kfl
// rdax	lp2bl,1
// wrax	lp2bl,-1
// rdax	lp2al,kql
// rdax	lp1bl,1
// mulx	kfl
// rdax	lp2al,1
// wrax	lp2al,0
// 
// rdax	lp1ar,1
// mulx	kfl
// rdax	lp1br,1
// wrax	lp1br,-1
// rdax	lp1ar,kql
// rdax	forr,1
// mulx	kfl
// rdax	lp1ar,1
// wrax	lp1ar,0
// 
// rdax	lp2ar,1
// mulx	kfl
// rdax	lp2br,1
// wrax	lp2br,-1
// rdax	lp2ar,kql
// rdax	lp1br,1
// mulx	kfl
// rdax	lp2ar,1
// wrax	lp2ar,0
// 
// ;mix around filter at extremes:
// 
// rdax	lp2bl,-1
// rdax	fol,1
// mulx	lbyp
// rdax	lp2bl,1
// wrax	dacl,0
// 
// rdax	lp2br,-1
// rdax	forr,1
// mulx	lbyp
// rdax	lp2br,1
// wrax	dacr,0
public class DanceIrFlaL extends ElmProgram {
  public DanceIrFlaL() {
    super("DanceIrFlaL");
    setSamplerate(48000);
    int krt = REG0;
    int kin = REG1;
    int kmix = REG2;
    int lp1al = REG3;
    int lp1bl = REG4;
    int lp1ar = REG5;
    int lp1br = REG6;
    int lp2al = REG7;
    int lp2bl = REG8;
    int lp2ar = REG9;
    int lp2br = REG10;
    int stop = REG11;
    int pbyp = REG12;
    int fol = REG13;
    int forr = REG14;
    int rol = REG15;
    int ror = REG16;
    int kfl = REG17;
    int temp = REG18;
    int rmixl = REG19;
    int rmixr = REG20;
    int lbyp = REG21;
    allocDelayMem("ap1", 202);
    allocDelayMem("ap2", 541);
    allocDelayMem("dap1a", 2204);
    allocDelayMem("dap1b", 2701);
    allocDelayMem("del1", 4456);
    allocDelayMem("dap2a", 2532);
    allocDelayMem("dap2b", 2201);
    allocDelayMem("del2", 6325);
    allocDelayMem("fdell", 512);
    allocDelayMem("fdelr", 512);
    double kap = 0.6;
    double kql = -0.4;
    skip(SKP_RUN, 1);
    loadRampLFO(0, 0, 512);
    readRegister(POT0, 1.999);
    writeRegister(kmix, 0);
    readRegister(POT0, -1);
    scaleOffset(1, 0.999);
    scaleOffset(1.999, 0);
    writeRegister(kin, 0);
    readRegister(POT0, 1);
    writeRegister(krt, 1);
    scaleOffset(1, -0.5);
    skip(SKP_GEZ, 2);
    scaleOffset(0, 0.5);
    writeRegister(krt, 0);
    readRegister(POT2, 1);
    scaleOffset(0.35, -0.35);
    exp(1, 0);
    writeRegister(kfl, 0);
    readRegister(POT2, 1);
    scaleOffset(1, -0.999);
    exp(1, 0);
    writeRegister(lbyp, 0);
    readRegister(ADCL, 0.25);
    readRegister(ADCR, 0.25);
    mulx(kin);
    readDelay("ap1", 1.0, kap);
    writeAllpass("ap1", 0, -kap);
    readDelay("ap2", 1.0, kap);
    writeAllpass("ap2", 0, -kap);
    writeRegister(temp, 0);
    readDelay("del2", 1.0, 1);
    mulx(krt);
    readRegister(temp, 1);
    readDelay("dap1a", 1.0, kap);
    writeAllpass("dap1a", 0, -kap);
    readDelay("dap1b", 1.0, kap);
    writeAllpass("dap1b", 0, -kap);
    writeDelay("del1", 0, 0);
    readDelay("del1", 1.0, 1);
    mulx(krt);
    readRegister(temp, 1);
    readDelay("dap2a", 1.0, kap);
    writeAllpass("dap2a", 0, -kap);
    readDelay("dap2b", 1.0, kap);
    writeAllpass("dap2b", 0, -kap);
    writeDelay("del2", 0, 0);
    readRegister(ADCL, -1);
    readDelay("del1", 0, 1);
    mulx(POT0);
    readRegister(ADCL, 1);
    writeDelay("fdell", 0, 0);
    readRegister(ADCR, -1);
    readDelay("del2", 0, 1);
    mulx(POT0);
    readRegister(ADCR, 1);
    writeDelay("fdelr", 0, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_REG|CHO_COMPC, "fdell", 0);
    chorusReadDelay(CHO_LFO_RMP0, 0, "fdell", (int)(1));
    scaleOffset(0.707, 0);
    readDelay("fdell", 0, 0.707);
    writeRegister(fol, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_COMPC, "fdelr", 0);
    chorusReadDelay(CHO_LFO_RMP0, 0, "fdelr", (int)(1));
    scaleOffset(0.707, 0);
    readDelay("fdelr", 0, 0.707);
    writeRegister(forr, 0);
    chorusReadValue(CHO_LFO_RMP0);
    readRegister(POT1, -0.03);
    writeRegister(RMP0_RATE, 1);
    readRegister(lp1al, 1);
    mulx(kfl);
    readRegister(lp1bl, 1);
    writeRegister(lp1bl, -1);
    readRegister(lp1al, kql);
    readRegister(fol, 1);
    mulx(kfl);
    readRegister(lp1al, 1);
    writeRegister(lp1al, 0);
    readRegister(lp2al, 1);
    mulx(kfl);
    readRegister(lp2bl, 1);
    writeRegister(lp2bl, -1);
    readRegister(lp2al, kql);
    readRegister(lp1bl, 1);
    mulx(kfl);
    readRegister(lp2al, 1);
    writeRegister(lp2al, 0);
    readRegister(lp1ar, 1);
    mulx(kfl);
    readRegister(lp1br, 1);
    writeRegister(lp1br, -1);
    readRegister(lp1ar, kql);
    readRegister(forr, 1);
    mulx(kfl);
    readRegister(lp1ar, 1);
    writeRegister(lp1ar, 0);
    readRegister(lp2ar, 1);
    mulx(kfl);
    readRegister(lp2br, 1);
    writeRegister(lp2br, -1);
    readRegister(lp2ar, kql);
    readRegister(lp1br, 1);
    mulx(kfl);
    readRegister(lp2ar, 1);
    writeRegister(lp2ar, 0);
    readRegister(lp2bl, -1);
    readRegister(fol, 1);
    mulx(lbyp);
    readRegister(lp2bl, 1);
    writeRegister(DACL, 0);
    readRegister(lp2br, -1);
    readRegister(forr, 1);
    mulx(lbyp);
    readRegister(lp2br, 1);
    writeRegister(DACR, 0);
  }
}
