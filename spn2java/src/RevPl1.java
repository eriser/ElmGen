import org.andrewkilpatrick.elmGen.ElmProgram;
// ;plate reverb, lush (large), stereo
// ;characterized by very high density,
// ;explosive initial sound, large
// ;pot0 = reverb time
// ;pot1 = lf loss
// ;pot2 = hf loss
// 
// mem	api1l	224	;left input all passes
// mem	api2l	430
// mem	api3l	856
// mem	api4l	1089
// 
// mem	api1r	156	;right input all passes
// mem	api2r	530
// mem	api3r	956
// mem	api4r	1289
// 
// mem	apd1	2301	;loop all passes
// mem	apd2	2902
// mem	apd3	3171
// mem	apd4	2401
// 
// mem	del1	3620	;loop delays
// mem	del2	4591
// mem	del3	4387
// mem	del4	3679
// 
// equ	temp	reg0
// equ	krt	reg1
// equ	ksh	reg2
// equ	ksl	reg3
// equ	lap	reg4
// equ	rap	reg5
// equ	hp1	reg6
// equ	hp2	reg7
// equ	hp3	reg8
// equ	hp4	reg9
// equ	lp1	reg10
// equ	lp2	reg11
// equ	lp3	reg12
// equ	lp4	reg13
// equ	lup	reg14
// 
// equ	kapi	0.6	;input AP coefficients
// equ	kap	0.6	;loop AP coefficients
// equ	kfl	0.8	;loop filter LPF freq
// equ	kfh	0.02	;loop filter HPF freq
// 
// ;now derive control coefficients from pots:
// 
// rdax	pot0,1
// log	0.5,0
// exp	1,0		;square root pot0
// sof	0.6,0.3		;ranges 0.3 to 0.9
// wrax	krt,0		;write for later use
// 
// rdax	pot1,1		;control low freq loss (high pass filter)
// sof	1,-0.999	;make from -1 to 0
// wrax	ksh,0		;high pass shelf
// 
// rdax	pot2,1		;control high freq loss (low pass filter)
// sof	1,-0.999	;make from -1 to 0
// wrax	ksl,0		;low pass shelf
// 
// ;now do input all passes, leave headroom:
// 
// rdax	adcl,0.25
// rda	api1l#,kapi
// wrap	api1l,-kapi
// rda	api2l#,kapi
// wrap	api2l,-kapi
// rda	api3l#,kapi
// wrap	api3l,-kapi
// rda	api4l#,kapi
// wrap	api4l,-kapi
// wrax	lap,0
// 
// rdax	adcr,0.25
// rda	api1r#,kapi
// wrap	api1r,-kapi
// rda	api2r#,kapi
// wrap	api2r,-kapi
// rda	api3r#,kapi
// wrap	api3r,-kapi
// rda	api4r#,kapi
// wrap	api4r,-kapi
// wrax	rap,1
// 
// ;all passed inputs in place, now process the loop, with filtering:
// 
// rdax	lup,1		;get signal from end of loop
// rda	apd1#,kap
// wrap	apd1,-kap	;do loop all pass
// wra	del1,0		;write delay
// rda	del1#,1		;read delay
// wrax	temp,1		;save in temp reg
// rdfx	hp1,kfh		;do low pass filter
// wrlx	hp1,-1		;infinite shelf LPF
// mulx	ksh		;prepare to subtract from temp
// rdax	temp,1		;subtract LPF from input (becomes HPF)
// wrax	temp,1		;save in temp reg
// rdfx	lp1,kfl		;do high pass filter
// wrhx	lp1,-1		;infinite shelf HPF
// mulx	ksl		;prepare to subtract from temp
// rdax	temp,1		;subtract HP signal from input (LPF shelf)
// mulx	krt		;scale by reverb time
// ;continue to next stage of loop
// rdax	lap,1
// rda	apd2#,kap
// wrap	apd2,-kap
// wra	del2,0
// rda	del2#,1
// wrax	temp,1
// rdfx	hp2,kfh
// wrlx	hp2,-1
// mulx	ksh
// rdax	temp,1
// wrax	temp,1
// rdfx	lp2,kfl
// wrhx	lp2,-1
// mulx	ksl
// rdax	temp,1
// mulx	krt
// 
// rdax	rap,1
// rda	apd3#,kap
// wrap	apd3,-kap
// wra	del3,0
// rda	del3#,1
// wrax	temp,1
// rdfx	hp3,kfh
// wrlx	hp3,-1
// mulx	ksh
// rdax	temp,1
// wrax	temp,1
// rdfx	lp3,kfl
// wrhx	lp3,-1
// mulx	ksl
// rdax	temp,1
// mulx	krt
// 
// rdax	lap,1
// rda	apd4#,kap
// wrap	apd4,-kap
// wra	del4,0
// rda	del4#,1
// wrax	temp,1
// rdfx	hp4,kfh
// wrlx	hp4,-1
// mulx	ksh
// rdax	temp,1
// wrax	temp,1
// rdfx	lp4,kfl
// wrhx	lp4,-1
// mulx	ksl
// rdax	temp,1
// mulx	krt
// wrax	lup,0
// 
// ;now gather outputs from loop delays:
// 
// rda	del1+201,0.8
// rda	del2+1345,0.7
// rda	del3+897,0.6
// rda	del4+1780,0.5
// wrax	dacl,0
// 
// rda	del1+1201,0.8
// rda	del2+145,0.7
// rda	del3+487,0.6
// rda	del4+780,0.5
// wrax	dacr,0
// 
// ;now generate a pair of LFOs to modulate the APs in the loop:
// 
// skp	run,2
// wlds	0,12,37
// wlds	1,15,33
// 
// ;now affect each delay:
// 
// cho	rda,sin0,reg|sin|compc,apd1+40
// cho	rda,sin0,sin,apd1+41
// wra	apd1+80,0
// 
// cho	rda,sin0,cos|compc,apd2+40
// cho	rda,sin0,cos,apd2+41
// wra	apd2+80,0
// 
// cho	rda,sin1,reg|sin|compc,apd3+40
// cho	rda,sin0,sin,apd3+41
// wra	apd3+80,0
// 
// cho	rda,sin1,cos|compc,apd4+40
// cho	rda,sin0,cos,apd4+41
// wra	apd4+80,0
public class RevPl1 extends ElmProgram {
  public RevPl1() {
    super("RevPl1");
    setSamplerate(48000);
    allocDelayMem("api1l", 224);
    allocDelayMem("api2l", 430);
    allocDelayMem("api3l", 856);
    allocDelayMem("api4l", 1089);
    allocDelayMem("api1r", 156);
    allocDelayMem("api2r", 530);
    allocDelayMem("api3r", 956);
    allocDelayMem("api4r", 1289);
    allocDelayMem("apd1", 2301);
    allocDelayMem("apd2", 2902);
    allocDelayMem("apd3", 3171);
    allocDelayMem("apd4", 2401);
    allocDelayMem("del1", 3620);
    allocDelayMem("del2", 4591);
    allocDelayMem("del3", 4387);
    allocDelayMem("del4", 3679);
    int temp = REG0;
    int krt = REG1;
    int ksh = REG2;
    int ksl = REG3;
    int lap = REG4;
    int rap = REG5;
    int hp1 = REG6;
    int hp2 = REG7;
    int hp3 = REG8;
    int hp4 = REG9;
    int lp1 = REG10;
    int lp2 = REG11;
    int lp3 = REG12;
    int lp4 = REG13;
    int lup = REG14;
    double kapi = 0.6;
    double kap = 0.6;
    double kfl = 0.8;
    double kfh = 0.02;
    readRegister(POT0, 1);
    log(0.5, 0);
    exp(1, 0);
    scaleOffset(0.6, 0.3);
    writeRegister(krt, 0);
    readRegister(POT1, 1);
    scaleOffset(1, -0.999);
    writeRegister(ksh, 0);
    readRegister(POT2, 1);
    scaleOffset(1, -0.999);
    writeRegister(ksl, 0);
    readRegister(ADCL, 0.25);
    readDelay("api1l", 1.0, kapi);
    writeAllpass("api1l", 0, -kapi);
    readDelay("api2l", 1.0, kapi);
    writeAllpass("api2l", 0, -kapi);
    readDelay("api3l", 1.0, kapi);
    writeAllpass("api3l", 0, -kapi);
    readDelay("api4l", 1.0, kapi);
    writeAllpass("api4l", 0, -kapi);
    writeRegister(lap, 0);
    readRegister(ADCR, 0.25);
    readDelay("api1r", 1.0, kapi);
    writeAllpass("api1r", 0, -kapi);
    readDelay("api2r", 1.0, kapi);
    writeAllpass("api2r", 0, -kapi);
    readDelay("api3r", 1.0, kapi);
    writeAllpass("api3r", 0, -kapi);
    readDelay("api4r", 1.0, kapi);
    writeAllpass("api4r", 0, -kapi);
    writeRegister(rap, 1);
    readRegister(lup, 1);
    readDelay("apd1", 1.0, kap);
    writeAllpass("apd1", 0, -kap);
    writeDelay("del1", 0, 0);
    readDelay("del1", 1.0, 1);
    writeRegister(temp, 1);
    readRegisterFilter(hp1, kfh);
    writeRegisterLowshelf(hp1, -1);
    mulx(ksh);
    readRegister(temp, 1);
    writeRegister(temp, 1);
    readRegisterFilter(lp1, kfl);
    writeRegisterHighshelf(lp1, -1);
    mulx(ksl);
    readRegister(temp, 1);
    mulx(krt);
    readRegister(lap, 1);
    readDelay("apd2", 1.0, kap);
    writeAllpass("apd2", 0, -kap);
    writeDelay("del2", 0, 0);
    readDelay("del2", 1.0, 1);
    writeRegister(temp, 1);
    readRegisterFilter(hp2, kfh);
    writeRegisterLowshelf(hp2, -1);
    mulx(ksh);
    readRegister(temp, 1);
    writeRegister(temp, 1);
    readRegisterFilter(lp2, kfl);
    writeRegisterHighshelf(lp2, -1);
    mulx(ksl);
    readRegister(temp, 1);
    mulx(krt);
    readRegister(rap, 1);
    readDelay("apd3", 1.0, kap);
    writeAllpass("apd3", 0, -kap);
    writeDelay("del3", 0, 0);
    readDelay("del3", 1.0, 1);
    writeRegister(temp, 1);
    readRegisterFilter(hp3, kfh);
    writeRegisterLowshelf(hp3, -1);
    mulx(ksh);
    readRegister(temp, 1);
    writeRegister(temp, 1);
    readRegisterFilter(lp3, kfl);
    writeRegisterHighshelf(lp3, -1);
    mulx(ksl);
    readRegister(temp, 1);
    mulx(krt);
    readRegister(lap, 1);
    readDelay("apd4", 1.0, kap);
    writeAllpass("apd4", 0, -kap);
    writeDelay("del4", 0, 0);
    readDelay("del4", 1.0, 1);
    writeRegister(temp, 1);
    readRegisterFilter(hp4, kfh);
    writeRegisterLowshelf(hp4, -1);
    mulx(ksh);
    readRegister(temp, 1);
    writeRegister(temp, 1);
    readRegisterFilter(lp4, kfl);
    writeRegisterHighshelf(lp4, -1);
    mulx(ksl);
    readRegister(temp, 1);
    mulx(krt);
    writeRegister(lup, 0);
    readDelay("del1", (int)(201), 0.8);
    readDelay("del2", (int)(1345), 0.7);
    readDelay("del3", (int)(897), 0.6);
    readDelay("del4", (int)(1780), 0.5);
    writeRegister(DACL, 0);
    readDelay("del1", (int)(1201), 0.8);
    readDelay("del2", (int)(145), 0.7);
    readDelay("del3", (int)(487), 0.6);
    readDelay("del4", (int)(780), 0.5);
    writeRegister(DACR, 0);
    skip(SKP_RUN, 2);
    loadSinLFO(0, 12, 37);
    loadSinLFO(1, 15, 33);
    chorusReadDelay(CHO_LFO_SIN0, CHO_REG|CHO_SIN|CHO_COMPC, "apd1", (int)(40));
    chorusReadDelay(CHO_LFO_SIN0, CHO_SIN, "apd1", (int)(41));
    writeDelay("apd1", (int)(80), 0);
    chorusReadDelay(CHO_LFO_SIN0, CHO_COS|CHO_COMPC, "apd2", (int)(40));
    chorusReadDelay(CHO_LFO_SIN0, CHO_COS, "apd2", (int)(41));
    writeDelay("apd2", (int)(80), 0);
    chorusReadDelay(CHO_LFO_SIN1, CHO_REG|CHO_SIN|CHO_COMPC, "apd3", (int)(40));
    chorusReadDelay(CHO_LFO_SIN0, CHO_SIN, "apd3", (int)(41));
    writeDelay("apd3", (int)(80), 0);
    chorusReadDelay(CHO_LFO_SIN1, CHO_COS|CHO_COMPC, "apd4", (int)(40));
    chorusReadDelay(CHO_LFO_SIN0, CHO_COS, "apd4", (int)(41));
    writeDelay("apd4", (int)(80), 0);
  }
}
