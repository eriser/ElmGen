import org.andrewkilpatrick.elmGen.ElmProgram;
// ;dance patchfor disco mixers:
// ;pot1 = Reverb to infinite RT, scales in and out levels
// ;pot2 = High pass filter (2 pole peaking, 8 ocatves)
// ;pot3 = Low pass filter (2 pole peaking, 8 ocatves)
// 
// ;filters are great for actively modifying program material;
// ;reveb can capture tonality for filter manipulation.
// ;beware, infinite reverb turns off input!
// 
// equ	krt	reg0
// equ	kin	reg1
// equ	kmix	reg2
// equ	hpal	reg3
// equ	hpbl	reg4
// equ	lpal	reg5
// equ	lpbl	reg6
// equ	hpar	reg7
// equ	hpbr	reg8
// equ	lpar	reg9
// equ	lpbr	reg10
// equ	kfh	reg11
// equ	kfl	reg12
// equ	temp	reg13
// equ	rmixl	reg14
// equ	rmixr	reg15
// equ	hpoutl	reg16
// equ	hpoutr	reg17
// equ	hbyp	reg18
// equ	lbyp	reg19
// 
// mem	ap1	202
// mem	ap2	541
// mem	ap3	1157
// mem	ap4	1903
// 
// mem	dap1a	2204
// mem	dap1b	3301
// mem	del1	4456
// mem	dap2a	3532
// mem	dap2b	3201
// mem	del2	6325
// 
// equ	kap	0.6
// equ	kql	-0.2
// equ	kqh	-0.2
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
// ;now prepare pot1 for HP sweeping.
// ;both frequency controls are exponential, and frequency increases
// ;with clockwise pot rotation. Target Kf ranges are from .001 to 1.0
// 
// clr
// rdax	pot1,1		;get pot1
// sof	0.5,-0.5	;ranges -0.5 to 0
// exp	1,0
// wrax	kfh,0		;write to HP filter control
// 
// rdax	pot2,1		;get pot2
// sof	0.5,-0.5	;ranges -0.5 to 0
// exp	1,0
// wrax	kfl,0		;write to LP filter control
// 
// ;now derive filter bypass functions (at open conditions)
// 
// rdax	pot1,-1
// sof	1,0.999		;ranges +1 to 0
// wrax	temp,1
// mulx	temp
// mulx	temp
// wrax	hbyp,0
// 
// 
// rdax	pot2,1		;read pot2 (LP) again
// mulx 	pot2
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
// rda	ap3#,kap
// wrap	ap3,-kap
// rda	ap4#,kap
// wrap	ap4,-kap
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
// rda	del1,1.5
// mulx	pot0
// rdax	adcl,1
// wrax	rmixl,0
// 
// rdax	adcr,-1
// rda	del2,1.5
// mulx	pot0
// rdax	adcr,1
// wrax	rmixr,0
// 
// ;Reverb outputs are at rmixl and rmixr.
// 
// ;now do two filters, start with the high pass, stereo.
// ;use the reveb mix for inputs, cascade the filter banks.
// 
// rdax	hpal,1
// mulx	kfh
// rdax	hpbl,1
// wrax	hpbl,-1
// rdax	hpal,kqh
// rdax	rmixl,1
// wrax	hpoutl,1	;HP output
// mulx	kfh
// rdax	hpal,1
// wrax	hpal,0
// 
// rdax	hpar,1
// mulx	kfh
// rdax	hpbr,1
// wrax	hpbr,-1
// rdax	hpar,kqh
// rdax	rmixr,1
// wrax	hpoutr,1	;HP output
// mulx	kfh
// rdax	hpar,1
// wrax	hpar,0
// 
// ;bypass if pot1 is fully counterclockwise:
// 
// rdax	hpoutl,-1
// rdax	rmixl,1
// mulx	hbyp
// rdax	hpoutl,1
// wrax	hpoutl,0
// 
// rdax	hpoutr,-1
// rdax	rmixr,1
// mulx	hbyp
// rdax	hpoutr,1
// wrax	hpoutr,0
// 
// ;now do cascaded low pass:
// 
// rdax	lpal,1
// mulx	kfl
// rdax	lpbl,1
// wrax	lpbl,-1
// rdax	lpal,kql
// rdax	hpoutl,1
// mulx	kfl
// rdax	lpal,1
// wrax	lpal,0
// 
// rdax	lpar,1
// mulx	kfl
// rdax	lpbr,1
// wrax	lpbr,-1
// rdax	lpar,kql
// rdax	hpoutr,1
// mulx	kfl
// rdax	lpar,1
// wrax	lpar,0
// 
// rdax	lpbl,-1
// rdax	hpoutl,1
// mulx	lbyp
// rdax	lpbl,1
// wrax	dacl,0
// 
// rdax	lpbr,-1
// rdax	hpoutr,1
// mulx	lbyp
// rdax	lpbr,1
// wrax	dacr,0
// 
// 
// 
// 
public class DanceIrHL extends ElmProgram {
  public DanceIrHL() {
    super("DanceIrHL");
    setSamplerate(48000);
    int krt = REG0;
    int kin = REG1;
    int kmix = REG2;
    int hpal = REG3;
    int hpbl = REG4;
    int lpal = REG5;
    int lpbl = REG6;
    int hpar = REG7;
    int hpbr = REG8;
    int lpar = REG9;
    int lpbr = REG10;
    int kfh = REG11;
    int kfl = REG12;
    int temp = REG13;
    int rmixl = REG14;
    int rmixr = REG15;
    int hpoutl = REG16;
    int hpoutr = REG17;
    int hbyp = REG18;
    int lbyp = REG19;
    allocDelayMem("ap1", 202);
    allocDelayMem("ap2", 541);
    allocDelayMem("ap3", 1157);
    allocDelayMem("ap4", 1903);
    allocDelayMem("dap1a", 2204);
    allocDelayMem("dap1b", 3301);
    allocDelayMem("del1", 4456);
    allocDelayMem("dap2a", 3532);
    allocDelayMem("dap2b", 3201);
    allocDelayMem("del2", 6325);
    double kap = 0.6;
    double kql = -0.2;
    double kqh = -0.2;
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
    readRegister(POT1, 1);
    scaleOffset(0.5, -0.5);
    exp(1, 0);
    writeRegister(kfh, 0);
    readRegister(POT2, 1);
    scaleOffset(0.5, -0.5);
    exp(1, 0);
    writeRegister(kfl, 0);
    readRegister(POT1, -1);
    scaleOffset(1, 0.999);
    writeRegister(temp, 1);
    mulx(temp);
    mulx(temp);
    writeRegister(hbyp, 0);
    readRegister(POT2, 1);
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
    readDelay("ap3", 1.0, kap);
    writeAllpass("ap3", 0, -kap);
    readDelay("ap4", 1.0, kap);
    writeAllpass("ap4", 0, -kap);
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
    readDelay("del1", 0, 1.5);
    mulx(POT0);
    readRegister(ADCL, 1);
    writeRegister(rmixl, 0);
    readRegister(ADCR, -1);
    readDelay("del2", 0, 1.5);
    mulx(POT0);
    readRegister(ADCR, 1);
    writeRegister(rmixr, 0);
    readRegister(hpal, 1);
    mulx(kfh);
    readRegister(hpbl, 1);
    writeRegister(hpbl, -1);
    readRegister(hpal, kqh);
    readRegister(rmixl, 1);
    writeRegister(hpoutl, 1);
    mulx(kfh);
    readRegister(hpal, 1);
    writeRegister(hpal, 0);
    readRegister(hpar, 1);
    mulx(kfh);
    readRegister(hpbr, 1);
    writeRegister(hpbr, -1);
    readRegister(hpar, kqh);
    readRegister(rmixr, 1);
    writeRegister(hpoutr, 1);
    mulx(kfh);
    readRegister(hpar, 1);
    writeRegister(hpar, 0);
    readRegister(hpoutl, -1);
    readRegister(rmixl, 1);
    mulx(hbyp);
    readRegister(hpoutl, 1);
    writeRegister(hpoutl, 0);
    readRegister(hpoutr, -1);
    readRegister(rmixr, 1);
    mulx(hbyp);
    readRegister(hpoutr, 1);
    writeRegister(hpoutr, 0);
    readRegister(lpal, 1);
    mulx(kfl);
    readRegister(lpbl, 1);
    writeRegister(lpbl, -1);
    readRegister(lpal, kql);
    readRegister(hpoutl, 1);
    mulx(kfl);
    readRegister(lpal, 1);
    writeRegister(lpal, 0);
    readRegister(lpar, 1);
    mulx(kfl);
    readRegister(lpbr, 1);
    writeRegister(lpbr, -1);
    readRegister(lpar, kql);
    readRegister(hpoutr, 1);
    mulx(kfl);
    readRegister(lpar, 1);
    writeRegister(lpar, 0);
    readRegister(lpbl, -1);
    readRegister(hpoutl, 1);
    mulx(lbyp);
    readRegister(lpbl, 1);
    writeRegister(DACL, 0);
    readRegister(lpbr, -1);
    readRegister(hpoutr, 1);
    mulx(lbyp);
    readRegister(lpbr, 1);
    writeRegister(DACR, 0);
  }
}
