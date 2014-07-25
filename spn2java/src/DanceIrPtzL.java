import org.andrewkilpatrick.elmGen.ElmProgram;
// ;disco mixer program
// ;pot0 = infinite reverb
// ;pot1 = pitch to zero
// ;pot2 = 4 pole low pass filter
// 
// equ	krt	reg0
// equ	kin	reg1
// equ	kmix	reg2
// equ	lpal	reg4
// equ	lpbl	reg5
// equ	lpar	reg6
// equ	lpbr	reg7
// equ	stop	reg8
// equ	pbyp	reg9
// equ	pol	reg10
// equ	por	reg11
// equ	kfl	reg12
// equ	temp	reg13
// equ	rmixl	reg14
// equ	rmixr	reg15
// equ	lbyp	reg16
// 
// 
// mem	ap1	502
// mem	ap2	821
// 
// mem	dap1a	2204
// mem	dap1b	2701
// mem	del1	4456
// mem	dap2a	2532
// mem	dap2b	2201
// mem	del2	6325
// 
// mem	pdelr	4096
// mem	pdell	4096
// mem	dtemp	1
// 
// equ	kap	0.6
// equ	kql	-0.2
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
// ;now prepare pot1 for pitch to zero.
// ;counter clockwise is full stop, clockwise is normal run
// 
// clr			;clr ACC from previous skp op
// rdax 	pot1,0.5
// sof 	1,-0.5		;(pot cannot go to full 1.0) -0.5 to 0
// wrax 	rmp0_rate,0
// 
// ;prepare stop signal, which shuts off the signal at the
// ;stop end of the pot range:
// 
// rdax	pot1,1.999
// sof	-2,0
// sof	-2,0
// wrax	stop,0
// 
// ;and a bypass value at norm pitch:
// 
// rdax	pot1,1
// sof	1,-1
// exp	1,0
// wrax	pbyp,0
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
// mulx 	pot2
// mulx	pot2
// mulx	pot2
// mulx	pot2
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
// rda	del2#,1
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
// rda	del1,1.9
// mulx	pot0
// rdax	adcl,1
// wrax	rmixl,0
// 
// rdax	adcr,-1
// rda	del2,1.9
// mulx	pot0
// rdax	adcr,1
// wrax	rmixr,0
// 
// ;Reverb outputs are at rmixl and rmixr.
// ;now do pitch to zero:
// 
// skp	run,1
// wldr	0,0,4096
// 
// rdax	rmixl,1
// wra	pdell,0
// 
// cho 	rda,rmp0,reg|compc,pdell
// cho 	rda,rmp0,0,pdell+1
// wra 	dtemp,0
// cho 	rda,rmp0,rptr2|compc,pdell
// cho 	rda,rmp0,rptr2,pdell+1
// cho 	sof,rmp0,na|compc,0
// cho 	rda,rmp0,na,dtemp
// mulx	stop
// wrax	temp,-1
// rdax	rmixl,1
// mulx	pbyp
// rdax	temp,1
// wrax 	pol,0
// 
// rdax	rmixr,1
// wra	pdelr,0
// 
// cho 	rda,rmp0,reg|compc,pdelr
// cho 	rda,rmp0,0,pdelr+1
// wra 	dtemp,0
// cho 	rda,rmp0,rptr2|compc,pdelr
// cho 	rda,rmp0,rptr2,pdelr+1
// cho 	sof,rmp0,na|compc,0
// cho 	rda,rmp0,na,dtemp
// mulx	stop
// wrax	temp,-1
// rdax	rmixr,1
// mulx	pbyp
// rdax	temp,1
// wrax	por,0
// 
// rdax	lpal,1
// mulx	kfl
// rdax	lpbl,1
// wrax	lpbl,-1
// rdax	lpal,kql
// rdax	pol,1
// mulx	kfl
// rdax	lpal,1
// wrax	lpal,0
// 
// rdax	lpar,1
// mulx	kfl
// rdax	lpbr,1
// wrax	lpbr,-1
// rdax	lpar,kql
// rdax	por,1
// mulx	kfl
// rdax	lpar,1
// wrax	lpar,0
// 
// rdax	lpbl,-1
// rdax	pol,1
// mulx	lbyp
// rdax	lpbl,1
// wrax	dacl,0
// 
// rdax	lpbr,-1
// rdax	por,1
// mulx	lbyp
// rdax	lpbr,1
// wrax	dacr,0
public class DanceIrPtzL extends ElmProgram {
  public DanceIrPtzL() {
    super("DanceIrPtzL");
    setSamplerate(48000);
    int krt = REG0;
    int kin = REG1;
    int kmix = REG2;
    int lpal = REG4;
    int lpbl = REG5;
    int lpar = REG6;
    int lpbr = REG7;
    int stop = REG8;
    int pbyp = REG9;
    int pol = REG10;
    int por = REG11;
    int kfl = REG12;
    int temp = REG13;
    int rmixl = REG14;
    int rmixr = REG15;
    int lbyp = REG16;
    allocDelayMem("ap1", 502);
    allocDelayMem("ap2", 821);
    allocDelayMem("dap1a", 2204);
    allocDelayMem("dap1b", 2701);
    allocDelayMem("del1", 4456);
    allocDelayMem("dap2a", 2532);
    allocDelayMem("dap2b", 2201);
    allocDelayMem("del2", 6325);
    allocDelayMem("pdelr", 4096);
    allocDelayMem("pdell", 4096);
    allocDelayMem("dtemp", 1);
    double kap = 0.6;
    double kql = -0.2;
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
    clear();
    readRegister(POT1, 0.5);
    scaleOffset(1, -0.5);
    writeRegister(RMP0_RATE, 0);
    readRegister(POT1, 1.999);
    scaleOffset(-2, 0);
    scaleOffset(-2, 0);
    writeRegister(stop, 0);
    readRegister(POT1, 1);
    scaleOffset(1, -1);
    exp(1, 0);
    writeRegister(pbyp, 0);
    readRegister(POT2, 1);
    scaleOffset(0.35, -0.35);
    exp(1, 0);
    writeRegister(kfl, 0);
    readRegister(POT2, 1);
    mulx(POT2);
    mulx(POT2);
    mulx(POT2);
    mulx(POT2);
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
    readDelay("del1", 0, 1.9);
    mulx(POT0);
    readRegister(ADCL, 1);
    writeRegister(rmixl, 0);
    readRegister(ADCR, -1);
    readDelay("del2", 0, 1.9);
    mulx(POT0);
    readRegister(ADCR, 1);
    writeRegister(rmixr, 0);
    skip(SKP_RUN, 1);
    loadRampLFO(0, 0, 4096);
    readRegister(rmixl, 1);
    writeDelay("pdell", 0, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_REG|CHO_COMPC, "pdell", 0);
    chorusReadDelay(CHO_LFO_RMP0, 0, "pdell", (int)(1));
    writeDelay("dtemp", 0, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_RPTR2|CHO_COMPC, "pdell", 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_RPTR2, "pdell", (int)(1));
    chorusScaleOffset(CHO_LFO_RMP0, CHO_NA|CHO_COMPC, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_NA, "dtemp", 0);
    mulx(stop);
    writeRegister(temp, -1);
    readRegister(rmixl, 1);
    mulx(pbyp);
    readRegister(temp, 1);
    writeRegister(pol, 0);
    readRegister(rmixr, 1);
    writeDelay("pdelr", 0, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_REG|CHO_COMPC, "pdelr", 0);
    chorusReadDelay(CHO_LFO_RMP0, 0, "pdelr", (int)(1));
    writeDelay("dtemp", 0, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_RPTR2|CHO_COMPC, "pdelr", 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_RPTR2, "pdelr", (int)(1));
    chorusScaleOffset(CHO_LFO_RMP0, CHO_NA|CHO_COMPC, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_NA, "dtemp", 0);
    mulx(stop);
    writeRegister(temp, -1);
    readRegister(rmixr, 1);
    mulx(pbyp);
    readRegister(temp, 1);
    writeRegister(por, 0);
    readRegister(lpal, 1);
    mulx(kfl);
    readRegister(lpbl, 1);
    writeRegister(lpbl, -1);
    readRegister(lpal, kql);
    readRegister(pol, 1);
    mulx(kfl);
    readRegister(lpal, 1);
    writeRegister(lpal, 0);
    readRegister(lpar, 1);
    mulx(kfl);
    readRegister(lpbr, 1);
    writeRegister(lpbr, -1);
    readRegister(lpar, kql);
    readRegister(por, 1);
    mulx(kfl);
    readRegister(lpar, 1);
    writeRegister(lpar, 0);
    readRegister(lpbl, -1);
    readRegister(pol, 1);
    mulx(lbyp);
    readRegister(lpbl, 1);
    writeRegister(DACL, 0);
    readRegister(lpbr, -1);
    readRegister(por, 1);
    mulx(lbyp);
    readRegister(lpbr, 1);
    writeRegister(DACR, 0);
  }
}
