import org.andrewkilpatrick.elmGen.ElmProgram;
// ;Key change and reverb program for karaoke applications.
// 
// ;Music (mono) in on left channel (perhaps from vocal remover)
// ;Microphone in on right channel
// 
// ;outputs are stereo reverb and synthesized stereo music.
// 
// ;pot0 controls key
// ;pot1 controls reverb amount
// ;pot2 controls reverb time
// 
// mem	pdel	4096		;Key change delay
// mem	dtemp	1		;temporary DRAM data location
// 
// mem	ap1	1156		;declare reverb input all-passes
// mem	ap2	1278
// mem	ap3	543
// mem	ap4	871
// 
// mem	dap1	2983		;declare loop APs and delays
// mem	del1	3678
// mem	dap2	2345
// mem	del2	2903
// mem	dap3	1883
// mem	del3	4878
// mem	dap4	2345
// mem	del4	2203
// 
// mem	sdel	1000		;stereo synth delay
// 
// equ	krt	reg0
// equ	loop	reg1
// equ	apout	reg2
// equ	slout	reg3
// equ	srout	reg4
// 
// ;constants:
// 
// equ	kap	0.625
// 
// ;PROGRAM:
// 
// skp	RUN,3
// wldr 	rmp0,0,2048			;Load up ramp LFO for key shift
// wlds	sin0,50,50			;load chorus generators for reverb
// wlds	sin1,34,60
// 
// rdax 	adcl,1				;read music input
// wra 	pdel,0				;write to delay start
// 
// cho 	rda,rmp0,reg|compc,pdel		;(1-k)*sample[addr]
// cho 	rda,rmp0,0,pdel+1		;k*sample[addr+1] + ACC
// wra 	dtemp,0				;Save it off to memory and clear ACC
// cho 	rda,rmp0,rptr2|compc,pdel	;(1-k)*sample[addr+ half ramp]
// cho 	rda,rmp0,rptr2,pdel+1		;k*sample[addr+ half ramp + 1] + ACC
// cho 	sof, rmp0,na|compc,0		;Result in ACC, multiply it by (1-XFADE) coefficient
// cho 	rda,rmp0,na,dtemp		;Add in earlier value saved in memory, multiply saved value by XFADE coefficient
// wra 	sdel,0 				;Write it to stereo delay and clear ACC
// 
// 
// rdax 	pot0,1.0		;get pot 0 (key control)
// sof 	0.25,-0.125		;scale for reasonable pitch range
// wrax 	rmp0_rate,0		;write to rate of lfo2, clear accumulator
// 
// ;now do reverb on microphone (right input):
// 
// ;prepare pot2 for reverb time:
// 
// rdax	pot2,1
// sof	0.4,0.4		;ranges 0.4 to 0.8
// wrax	krt,0
// 
// ;do input all passes, bring in signal down 12dB to allow headroom:
// 
// rdax	adcr,0.25
// rda	ap1#,kap
// wrap	ap1,-kap
// rda	ap2#,kap
// wrap	ap2,-kap
// rda	ap3#,kap
// wrap	ap3,-kap
// rda	ap4#,kap
// wrap	ap4,-kap
// wrax	apout,1		;save all pass result in apout and pass on
// 
// ;now do reverb loop:
// 
// rdax	loop,1
// rda	dap1#,kap
// wrap	dap1,-kap
// wra	del1,0
// 
// rda	del1#,1
// mulx	krt
// rdax	apout,1
// rda	dap2#,kap
// wrap	dap2,-kap
// wra	del2,0
// rda	del2#,1
// 
// mulx	krt
// rdax	apout,1
// rda	dap3#,kap
// wrap	dap3,-kap
// wra	del3,0
// 
// rda	del3#,1
// mulx	krt
// rdax	apout,1
// rda	dap4#,kap
// wrap	dap4,-kap
// wra	del4,0
// 
// rda	del4#,1
// mulx	krt
// wrax	loop,0
// 
// ;reverb outputs are at del1 and del3
// 
// ;now synthesize stereo from music program with delays:
// 
// rda	sdel,1
// rda	sdel#,1
// wrax	slout,0
// 
// rda	sdel,1
// rda	sdel#,-1
// wrax	srout,0
// 
// ;now combine input and reverb signals:
// 
// rda	del1,1.99	;read reverb output
// sof	1.999,0		;recover gain
// mulx	pot1		;reverb amount
// mulx	pot1		;square function
// rdax	slout,1		;add in pseudo-stereo program
// rdax 	adcr,1		;add vocal (dry)
// wrax	dacr,0		;write to left channel
// 
// rda	del3,1.99	;read reverb output
// sof	1.999,0		;recover gain
// mulx	pot1		;reverb amount
// mulx	pot1		;square function
// rdax	srout,1		;add in pseudo-stereo program
// rdax 	adcr,1		;add vocal (dry)
// wrax	dacl,0		;write to left channel
// 
// ;now smooth reverb with chorus:
// 
// cho	rda,sin0,reg|compc,del2+32
// cho	rda,sin0,0,del2+33
// wra	del2+64,0
// 
// cho	rda,sin0,cos|compc,del4+32
// cho	rda,sin0,cos,del4+33
// wra	del4+64,0
// 
// cho	rda,sin1,compc,del3+32
// cho	rda,sin1,0,del3+33
// wra	del3+64,0
// 
// cho	rda,sin1,cos|compc,del1+32
// cho	rda,sin1,cos,del1+33
// wra	del1+64,0
public class KeyRev extends ElmProgram {
  public KeyRev() {
    super("KeyRev");
    setSamplerate(48000);
    allocDelayMem("pdel", 4096);
    allocDelayMem("dtemp", 1);
    allocDelayMem("ap1", 1156);
    allocDelayMem("ap2", 1278);
    allocDelayMem("ap3", 543);
    allocDelayMem("ap4", 871);
    allocDelayMem("dap1", 2983);
    allocDelayMem("del1", 3678);
    allocDelayMem("dap2", 2345);
    allocDelayMem("del2", 2903);
    allocDelayMem("dap3", 1883);
    allocDelayMem("del3", 4878);
    allocDelayMem("dap4", 2345);
    allocDelayMem("del4", 2203);
    allocDelayMem("sdel", 1000);
    int krt = REG0;
    int loop = REG1;
    int apout = REG2;
    int slout = REG3;
    int srout = REG4;
    double kap = 0.625;
    skip(SKP_RUN, 3);
    loadRampLFO(0, 0, 2048);
    loadSinLFO(0, 50, 50);
    loadSinLFO(1, 34, 60);
    readRegister(ADCL, 1);
    writeDelay("pdel", 0, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_REG|CHO_COMPC, "pdel", 0);
    chorusReadDelay(CHO_LFO_RMP0, 0, "pdel", (int)(1));
    writeDelay("dtemp", 0, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_RPTR2|CHO_COMPC, "pdel", 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_RPTR2, "pdel", (int)(1));
    chorusScaleOffset(CHO_LFO_RMP0, CHO_NA|CHO_COMPC, 0);
    chorusReadDelay(CHO_LFO_RMP0, CHO_NA, "dtemp", 0);
    writeDelay("sdel", 0, 0);
    readRegister(POT0, 1.0);
    scaleOffset(0.25, -0.125);
    writeRegister(RMP0_RATE, 0);
    readRegister(POT2, 1);
    scaleOffset(0.4, 0.4);
    writeRegister(krt, 0);
    readRegister(ADCR, 0.25);
    readDelay("ap1", 1.0, kap);
    writeAllpass("ap1", 0, -kap);
    readDelay("ap2", 1.0, kap);
    writeAllpass("ap2", 0, -kap);
    readDelay("ap3", 1.0, kap);
    writeAllpass("ap3", 0, -kap);
    readDelay("ap4", 1.0, kap);
    writeAllpass("ap4", 0, -kap);
    writeRegister(apout, 1);
    readRegister(loop, 1);
    readDelay("dap1", 1.0, kap);
    writeAllpass("dap1", 0, -kap);
    writeDelay("del1", 0, 0);
    readDelay("del1", 1.0, 1);
    mulx(krt);
    readRegister(apout, 1);
    readDelay("dap2", 1.0, kap);
    writeAllpass("dap2", 0, -kap);
    writeDelay("del2", 0, 0);
    readDelay("del2", 1.0, 1);
    mulx(krt);
    readRegister(apout, 1);
    readDelay("dap3", 1.0, kap);
    writeAllpass("dap3", 0, -kap);
    writeDelay("del3", 0, 0);
    readDelay("del3", 1.0, 1);
    mulx(krt);
    readRegister(apout, 1);
    readDelay("dap4", 1.0, kap);
    writeAllpass("dap4", 0, -kap);
    writeDelay("del4", 0, 0);
    readDelay("del4", 1.0, 1);
    mulx(krt);
    writeRegister(loop, 0);
    readDelay("sdel", 0, 1);
    readDelay("sdel", 1.0, 1);
    writeRegister(slout, 0);
    readDelay("sdel", 0, 1);
    readDelay("sdel", 1.0, -1);
    writeRegister(srout, 0);
    readDelay("del1", 0, 1.99);
    scaleOffset(1.999, 0);
    mulx(POT1);
    mulx(POT1);
    readRegister(slout, 1);
    readRegister(ADCR, 1);
    writeRegister(DACR, 0);
    readDelay("del3", 0, 1.99);
    scaleOffset(1.999, 0);
    mulx(POT1);
    mulx(POT1);
    readRegister(srout, 1);
    readRegister(ADCR, 1);
    writeRegister(DACL, 0);
    chorusReadDelay(CHO_LFO_SIN0, CHO_REG|CHO_COMPC, "del2", (int)(32));
    chorusReadDelay(CHO_LFO_SIN0, 0, "del2", (int)(33));
    writeDelay("del2", (int)(64), 0);
    chorusReadDelay(CHO_LFO_SIN0, CHO_COS|CHO_COMPC, "del4", (int)(32));
    chorusReadDelay(CHO_LFO_SIN0, CHO_COS, "del4", (int)(33));
    writeDelay("del4", (int)(64), 0);
    chorusReadDelay(CHO_LFO_SIN1, CHO_COMPC, "del3", (int)(32));
    chorusReadDelay(CHO_LFO_SIN1, 0, "del3", (int)(33));
    writeDelay("del3", (int)(64), 0);
    chorusReadDelay(CHO_LFO_SIN1, CHO_COS|CHO_COMPC, "del1", (int)(32));
    chorusReadDelay(CHO_LFO_SIN1, CHO_COS, "del1", (int)(33));
    writeDelay("del1", (int)(64), 0);
  }
}
