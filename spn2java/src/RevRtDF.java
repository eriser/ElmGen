import org.andrewkilpatrick.elmGen.ElmProgram;
// ;Stereo reverb with 3 variables:
// ;pot0 = reverb time
// ;pot1 = diffusion
// ;pot2 = decay filtering
// ;95 ticks
// 
// equ	krt	reg0	;reverb time
// equ	kdiff	reg1	;input AP diffusion coefficients
// equ	kfh	reg2	;high freq shelf
// equ	kfl	reg3	;low freq shelf
// equ	temp1	reg4	;temporary register
// equ	temp2	reg5
// equ	temp3	reg6
// equ	fil_ll	reg7
// equ	fil_lh	reg8
// equ	fil_rl	reg9
// equ	fil_rh	reg10
// equ	kin	reg11	;input scaling, function of Krt
// 
// equ	kap	0.6	;loop AP coefficients
// equ	kapi	0.65	;input AP coefficient
// equ	kluph	0.3	;loop high cut freq
// equ	klupl	0.03	;loop low cut freq
// 
// mem	apl1	420
// mem	apl2	867
// mem	apl3	1578
// mem	apl4	390
// 
// mem	apr1	450
// mem	apr2	909
// mem	apr3	1630
// mem	apr4	576
// 
// mem	apcl1	3402
// mem	apcl2	2202
// mem	dcl	7678
// 
// mem	apcr1	3167
// mem	apcr2	2009
// mem	dcr	7353
// 
// ;prepare pots:
// 
// rdax	pot0,1
// log	0.5,0
// exp	1,0		;square root of pot
// sof	0.7,0.2		;rt spans 0.2 to 0.9
// wrax	krt,-0.8
// sof	1,0.999
// wrax	kin,0		;input scaling coefficient
// 
// 
// rdax	pot1,0.7
// wrax	kdiff,0		;input AP Ks from 0 to 0.7
// 
// sof	0,-0.5		;load -0.5
// rdax	pot2,1		;add pot2, ranges -0.5 to +0.5
// sof	1.9999,0	;ranges -1 to +1
// wrax	temp1,1
// skp	neg,1
// clr			;do not allow to go positive
// wrax	kfh,0		;store low shelf, -1 to 0 (at mid pos)
// rdax	temp1,1		;get pot back
// sof	-1,0		;change sign
// skp	neg,1
// clr
// wrax	kfl,0		;same with high shelf, 0 (at mid pos) to -1
// 
// ;now do reverb:
// 
// ;do 3 input all passes, controlled by the variable Kdiff
// 
// rdax	adcl,0.5	;read in left side, leave headroom
// mulx	kin
// wrax	temp3,0
// 
// rda	apl1#,1		;read first AP output
// mulx	kdiff		;mult by diffusion
// rdax	temp3,1		;read in left side, leave headroom
// wra	apl1,-1		;write result to AP delay, change sign
// mulx	kdiff		;mult by diffusion
// rda	apl1#,1		;read AP out again
// wrax	temp1,0		;save to temp and clear ACC
// 
// rda	apl2#,1
// mulx	kdiff
// rdax	temp1,1
// wra	apl2,-1
// mulx	kdiff
// rda	apl2#,1
// wrax	temp1,0
// 
// rda	apl3#,1
// mulx	kdiff
// rdax	temp1,1
// wra	apl3,-1
// mulx	kdiff
// rda	apl3#,1
// 
// rda	apl4#,kapi	;read fourth input AP, fixed coefficient
// wrap	apl4,-kapi
// rdax	temp1,0		;save in temp reg
// 
// ;input all passes are now done. Now we add in the loop signal and filter:
// 
// rda	dcr#,1		;read right delay output
// rdfx	fil_ll,klupl	;set up low pass filter for high pass operation
// wrax	fil_ll,1	;change sign on lpf output
// mulx	kfl		;scale by lower end of pot2 range
// rda	dcr#,1		;read back the delay output
// wrax	temp2,1		;save, continue in ACC
// rdfx	fil_lh,kluph	;do low pass for high end control
// wrhx	fil_lh,-1	;create infinite shelf high pass
// mulx	kfh		;scale by upper end of pot2 range
// rdax	temp2,1		;sum back temp value
// 
// ;Loop feedback is now in place, filtered.
// ;scale by Krt, sum in input AP signal.
// 
// mulx	krt
// rdax	temp1,1		;sum in input all pass filters
// rda	apcl1#,kap
// wrap	apcl1,-kap	;do left loop all pass
// rda	apcl2#,kap
// wrap	apcl2,-kap	;do left loop all pass
// wra	dcl,0		;write result to left loop delay
// 
// ;now do the same to the right channel:
// 
// rdax	adcr,0.5	;read in right side, leave headroom
// mulx	kin
// wrax	temp3,0
// 
// rda	apr1#,1
// mulx	kdiff
// rdax	temp3,1
// wra	apr1,-1
// mulx	kdiff
// rda	apr1#,1
// wrax	temp1,0
// 
// rda	apr2#,1
// mulx	kdiff
// rdax	temp1,1
// wra	apr2,-1
// mulx	kdiff
// rda	apr2#,1
// wrax	temp1,0
// 
// rda	apr3#,1
// mulx	kdiff
// rdax	temp1,1
// wra	apr3,-1
// mulx	kdiff
// rda	apr3#,1
// 
// rda	apr4#,kapi	;read fourth input AP, fixed coefficient
// wrap	apr4,-kapi
// rdax	temp1,0		;save in temp reg
// 
// ;input all passes are now done. Now we add in the loop signal and filter:
// 
// rda	dcl#,1
// rdfx	fil_rl,klupl
// wrax	fil_rl,1
// mulx	kfl
// rda	dcl#,1
// wrax	temp2,1
// rdfx	fil_rh,kluph
// wrhx	fil_rh,-1
// mulx	kfh
// rdax	temp2,1
// 
// ;Loop feedback is now in place, filtered. Now sum in input AP signal.
// 
// mulx	krt
// rdax	temp1,1		;sum in input all pass filters
// rda	apcr1#,kap
// wrap	apcr1,-kap
// rda	apcr2#,kap
// wrap	apcr2,-kap
// wra	dcr,0		;write result to right loop delay
// 
// ;now grab outputs from each delay input:
// 
// rda	dcl,1.99
// rda	dcl#,0.8
// rda	dcr#,0.9
// wrax	dacl,0
// rda	dcr,1.99
// rda	dcr#,0.8
// rda	dcl#,0.9
// wrax	dacr,0
// clr
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
// 
public class RevRtDF extends ElmProgram {
  public RevRtDF() {
    super("RevRtDF");
    setSamplerate(48000);
    int krt = REG0;
    int kdiff = REG1;
    int kfh = REG2;
    int kfl = REG3;
    int temp1 = REG4;
    int temp2 = REG5;
    int temp3 = REG6;
    int fil_ll = REG7;
    int fil_lh = REG8;
    int fil_rl = REG9;
    int fil_rh = REG10;
    int kin = REG11;
    double kap = 0.6;
    double kapi = 0.65;
    double kluph = 0.3;
    double klupl = 0.03;
    allocDelayMem("apl1", 420);
    allocDelayMem("apl2", 867);
    allocDelayMem("apl3", 1578);
    allocDelayMem("apl4", 390);
    allocDelayMem("apr1", 450);
    allocDelayMem("apr2", 909);
    allocDelayMem("apr3", 1630);
    allocDelayMem("apr4", 576);
    allocDelayMem("apcl1", 3402);
    allocDelayMem("apcl2", 2202);
    allocDelayMem("dcl", 7678);
    allocDelayMem("apcr1", 3167);
    allocDelayMem("apcr2", 2009);
    allocDelayMem("dcr", 7353);
    readRegister(POT0, 1);
    log(0.5, 0);
    exp(1, 0);
    scaleOffset(0.7, 0.2);
    writeRegister(krt, -0.8);
    scaleOffset(1, 0.999);
    writeRegister(kin, 0);
    readRegister(POT1, 0.7);
    writeRegister(kdiff, 0);
    scaleOffset(0, -0.5);
    readRegister(POT2, 1);
    scaleOffset(1.9999, 0);
    writeRegister(temp1, 1);
    skip(SKP_NEG, 1);
    clear();
    writeRegister(kfh, 0);
    readRegister(temp1, 1);
    scaleOffset(-1, 0);
    skip(SKP_NEG, 1);
    clear();
    writeRegister(kfl, 0);
    readRegister(ADCL, 0.5);
    mulx(kin);
    writeRegister(temp3, 0);
    readDelay("apl1", 1.0, 1);
    mulx(kdiff);
    readRegister(temp3, 1);
    writeDelay("apl1", 0, -1);
    mulx(kdiff);
    readDelay("apl1", 1.0, 1);
    writeRegister(temp1, 0);
    readDelay("apl2", 1.0, 1);
    mulx(kdiff);
    readRegister(temp1, 1);
    writeDelay("apl2", 0, -1);
    mulx(kdiff);
    readDelay("apl2", 1.0, 1);
    writeRegister(temp1, 0);
    readDelay("apl3", 1.0, 1);
    mulx(kdiff);
    readRegister(temp1, 1);
    writeDelay("apl3", 0, -1);
    mulx(kdiff);
    readDelay("apl3", 1.0, 1);
    readDelay("apl4", 1.0, kapi);
    writeAllpass("apl4", 0, -kapi);
    readRegister(temp1, 0);
    readDelay("dcr", 1.0, 1);
    readRegisterFilter(fil_ll, klupl);
    writeRegister(fil_ll, 1);
    mulx(kfl);
    readDelay("dcr", 1.0, 1);
    writeRegister(temp2, 1);
    readRegisterFilter(fil_lh, kluph);
    writeRegisterHighshelf(fil_lh, -1);
    mulx(kfh);
    readRegister(temp2, 1);
    mulx(krt);
    readRegister(temp1, 1);
    readDelay("apcl1", 1.0, kap);
    writeAllpass("apcl1", 0, -kap);
    readDelay("apcl2", 1.0, kap);
    writeAllpass("apcl2", 0, -kap);
    writeDelay("dcl", 0, 0);
    readRegister(ADCR, 0.5);
    mulx(kin);
    writeRegister(temp3, 0);
    readDelay("apr1", 1.0, 1);
    mulx(kdiff);
    readRegister(temp3, 1);
    writeDelay("apr1", 0, -1);
    mulx(kdiff);
    readDelay("apr1", 1.0, 1);
    writeRegister(temp1, 0);
    readDelay("apr2", 1.0, 1);
    mulx(kdiff);
    readRegister(temp1, 1);
    writeDelay("apr2", 0, -1);
    mulx(kdiff);
    readDelay("apr2", 1.0, 1);
    writeRegister(temp1, 0);
    readDelay("apr3", 1.0, 1);
    mulx(kdiff);
    readRegister(temp1, 1);
    writeDelay("apr3", 0, -1);
    mulx(kdiff);
    readDelay("apr3", 1.0, 1);
    readDelay("apr4", 1.0, kapi);
    writeAllpass("apr4", 0, -kapi);
    readRegister(temp1, 0);
    readDelay("dcl", 1.0, 1);
    readRegisterFilter(fil_rl, klupl);
    writeRegister(fil_rl, 1);
    mulx(kfl);
    readDelay("dcl", 1.0, 1);
    writeRegister(temp2, 1);
    readRegisterFilter(fil_rh, kluph);
    writeRegisterHighshelf(fil_rh, -1);
    mulx(kfh);
    readRegister(temp2, 1);
    mulx(krt);
    readRegister(temp1, 1);
    readDelay("apcr1", 1.0, kap);
    writeAllpass("apcr1", 0, -kap);
    readDelay("apcr2", 1.0, kap);
    writeAllpass("apcr2", 0, -kap);
    writeDelay("dcr", 0, 0);
    readDelay("dcl", 0, 1.99);
    readDelay("dcl", 1.0, 0.8);
    readDelay("dcr", 1.0, 0.9);
    writeRegister(DACL, 0);
    readDelay("dcr", 0, 1.99);
    readDelay("dcr", 1.0, 0.8);
    readDelay("dcl", 1.0, 0.9);
    writeRegister(DACR, 0);
    clear();
  }
}
