import org.andrewkilpatrick.elmGen.ElmProgram;
// ;XOVER and response compensation for powered loudspeaker
// ;Crossover for 2-way system
// ;Mono input, HF and LF outputs
// ;24dB/oct crossover (Linkwitz-Riley)
// ;Tweeter delay in ~1/4" increments (Fs=48KHz)
// ;7 band parametric EQ for speaker matching
// ;Shelving low pass for bass boosting
// 
// ;Equations for setting EQ bands:
// 
// ;kp(x) = peak/dip; range from -1 (inf notch) to +1.9999 (+6dB)
// ;kf(x) = sqrt((4*kts)/(1+(kt/q)+kts))
// ;kq(x) = (1-(kt/q)+kts)/(1+(kt/q)+kts)
// ;kg(x) = (kt/q)/(1+(kt/q)+kts)
// 
// ;where:
// ;kt=tan(pi*f/Fs)
// ;kts=kt^2
// ;f=center frequency
// ;Fs=sample rate
// ;q=Q of filter peak
// 
// mem	del1	1000	;tweeter delay, floating point storage
// mem	del2	1000	;tweeter delay, FP error storage
// 
// equ	lf1a	reg0	;reg for low freq XOVER
// equ	lf1b	reg1	;reg for low freq XOVER
// equ	lf2a	reg2	;reg for low freq XOVER
// equ	lf2b	reg3	;reg for low freq XOVER (output)
// equ	hf1a	reg4	;reg for high freq XOVER
// equ	hf1b	reg5	;reg for high freq XOVER
// equ	hf2a	reg6	;reg for high freq XOVER
// equ	hf2b	reg7	;reg for high freq XOVER
// equ	temp	reg8	;reg high freq XOVER output and temp store
// equ	delout	reg9	;reg for tweeter delay output
// equ	eqin	reg10	;mono input signal to EQ section
// equ	b1a	reg11
// equ	b1b	reg12
// equ	b2a	reg13
// equ	b2b	reg14
// equ	b3a	reg15
// equ	b3b	reg16
// equ	b4a	reg17
// equ	b4b	reg18
// equ	b5a	reg19
// equ	b5b	reg20
// equ	b6a	reg21
// equ	b6b	reg22
// equ	b7a	reg23
// equ	b7b	reg24
// equ	loext	reg25		;extend low end with shelving low pass
// 
// ;standard system setup variables:
// 
// equ	del	0	;samples of delay in tweeter path
// equ	kfl	0.48	;XOVER low freq
// equ	kql	0.5	;XOVER low q
// equ	kfh	0.6	;XOVER high freq
// equ	kqh	0.85	;XOVER high q
// equ	kflext	0.01	;bass extension frequency
// equ	kshext	-0.5	;bass extension shelf
// 
// ;custom EQ variables:
// 
// equ	ampl	1.0	;woofer amplitude
// equ	amph	1.0	;tweeter amplitude
// ;EQ params will depend on driver set.
// equ	kf1	0	;band frequency
// equ	kq1	0	;band Q
// equ	kp1	0	;band peak (+6dB max), dip (-inf)
// equ	kg1	0
// 
// equ	kf2	0
// equ	kq2	0
// equ	kp2	0
// equ	kg2	0
// 
// equ	kf3	0
// equ	kq3	0
// equ	kp3	0
// equ	kg3	0
// 
// equ	kf4	0
// equ	kq4	0
// equ	kp4	0
// equ	kg4	0
// 
// equ	kf5	0
// equ	kq5	0
// equ	kp5	0
// equ	kg5	0
// 
// equ	kf6	0
// equ	kq6	0
// equ	kp6	0
// equ	kg6	0
// 
// equ	kf7	0
// equ	kq7	0
// equ	kp7	0
// equ	kg7	0
// 
// ;sum inputs to temp register:
// 
// rdax	adcr,0.5
// rdax	adcl,0.5
// wrax	eqin,0
// 
// ;Equalizer to correct amplitude variations.
// ;input to filter bank is in toeq, output will be input
// ;plus fractions of each band filter:
// 
// ;EQ band 1:
// 
// rdax	eqin,kg1
// rdax	b1b,-kf1
// rdax	b1a,1
// wrax	temp,kq1
// rdax	eqin,kg1
// wrax	b1a,0
// rdax	temp,kf1
// rdax	b1b,1
// wrax	b1b,0
// rdax	eqin,1
// rdax	temp,kp1
// wrax	eqin,kg2
// 
// rdax	b2b,-kf2
// rdax	b2a,1
// wrax	temp,kq2
// rdax	eqin,kg2
// wrax	b2a,0
// rdax	temp,kf2
// rdax	b2b,1
// wrax	b2b,0
// rdax	eqin,1
// rdax	temp,kp2
// wrax	eqin,kg3
// 
// rdax	b3b,-kf3
// rdax	b3a,1
// wrax	temp,kq3
// rdax	eqin,kg3
// wrax	b3a,0
// rdax	temp,kf3
// rdax	b3b,1
// wrax	b3b,0
// rdax	eqin,1
// rdax	temp,kp3
// wrax	eqin,kg4
// 
// rdax	b4b,-kf4
// rdax	b4a,1
// wrax	temp,kq4
// rdax	eqin,kg4
// wrax	b4a,0
// rdax	temp,kf4
// rdax	b4b,1
// wrax	b4b,0
// rdax	eqin,1
// rdax	temp,kp4
// wrax	eqin,kg5
// 
// rdax	b5b,-kf5
// rdax	b5a,1
// wrax	temp,kq5
// rdax	eqin,kg5
// wrax	b5a,0
// rdax	temp,kf5
// rdax	b5b,1
// wrax	b5b,0
// rdax	eqin,1
// rdax	temp,kp5
// wrax	eqin,kg6
// 
// rdax	b6b,-kf6
// rdax	b6a,1
// wrax	temp,kq6
// rdax	eqin,kg6
// wrax	b6a,0
// rdax	temp,kf6
// rdax	b6b,1
// wrax	b6b,0
// rdax	eqin,1
// rdax	temp,kp6
// wrax	eqin,kg7
// 
// rdax	b7b,-kf7
// rdax	b7a,1
// wrax	temp,kq7
// rdax	eqin,kg7
// wrax	b7a,0
// rdax	temp,kf7
// rdax	b7b,1
// wrax	b7b,0
// rdax	eqin,1
// rdax	temp,kp7
// wrax	eqin,1		;keep value in accumulator
// 
// 
// ;write to delay (for tweeter) and get delayed output:
// 
// wra	del1,1		;write fp value to del1
// rda	del1,-1		;subtract FP value from real value
// wra	del2,0		;write error value to del2
// 
// rda	del1+del,1	;read delayed FP value
// rda	del2+del,1	;add delayed error value
// wrax	delout,0	;wrtie value to delay output register.
// 
// ;do crossover, 24dB Linkwitz-Riley alignment
// ;Low pass filter for woofer:
// 
// rdax	lf1a,kfl
// rdax	lf1b,1
// wrax	lf1b,-kfl
// rdax	lf1a,kql
// rdax	eqin,0.05
// wrax	lf1a,0
// 
// rdax	lf2a,kfl
// rdax	lf2b,1
// wrax	lf2b,-kfl
// rdax	lf2a,kql
// rdax	lf1b,1
// wrax	lf2a,0
// 
// ;high pass filter for tweeter:
// 
// rdax	hf1a,kfh
// rdax	hf1b,1
// wrax	hf1b,1
// rdax	delout,0.25
// rdax	hf1a,kqh
// wrax	delout,1
// sof	-kfh,0
// rdax	hf1a,1
// wrax	hf1a,0
// 
// rdax	hf2a,kfh
// rdax	hf2b,1
// wrax	hf2b,1
// rdax	delout,0.3
// rdax	hf2a,kqh
// wrax	delout,1
// sof	-kfh,0
// rdax	hf2a,1
// wrax	hf2a,0
// 
// ;take outputs from crossover:
// 
// rdax	lf2b,-2
// rdfx	loext,kflext	;bass extension shelving filter
// wrlx	loext,kshext	;shelf
// sof	-2,0
// sof	ampl,0		;scale woofer amplitude
// wrax	dacl,0		;output woofer through left channel
// 
// rdax	delout,-2
// sof	-2,0
// sof	amph,0		;scale tweeter amplitude
// wrax	dacr,0		;output tweeter through right channel
// 
// 
// 
// 
// 
public class ActiveCrossover extends ElmProgram {
  public ActiveCrossover() {
    super("ActiveCrossover");
    setSamplerate(48000);
    allocDelayMem("del1", 1000);
    allocDelayMem("del2", 1000);
    int lf1a = REG0;
    int lf1b = REG1;
    int lf2a = REG2;
    int lf2b = REG3;
    int hf1a = REG4;
    int hf1b = REG5;
    int hf2a = REG6;
    int hf2b = REG7;
    int temp = REG8;
    int delout = REG9;
    int eqin = REG10;
    int b1a = REG11;
    int b1b = REG12;
    int b2a = REG13;
    int b2b = REG14;
    int b3a = REG15;
    int b3b = REG16;
    int b4a = REG17;
    int b4b = REG18;
    int b5a = REG19;
    int b5b = REG20;
    int b6a = REG21;
    int b6b = REG22;
    int b7a = REG23;
    int b7b = REG24;
    int loext = REG25;
    int del = 0;
    double kfl = 0.48;
    double kql = 0.5;
    double kfh = 0.6;
    double kqh = 0.85;
    double kflext = 0.01;
    double kshext = -0.5;
    double ampl = 1.0;
    double amph = 1.0;
    int kf1 = 0;
    int kq1 = 0;
    int kp1 = 0;
    int kg1 = 0;
    int kf2 = 0;
    int kq2 = 0;
    int kp2 = 0;
    int kg2 = 0;
    int kf3 = 0;
    int kq3 = 0;
    int kp3 = 0;
    int kg3 = 0;
    int kf4 = 0;
    int kq4 = 0;
    int kp4 = 0;
    int kg4 = 0;
    int kf5 = 0;
    int kq5 = 0;
    int kp5 = 0;
    int kg5 = 0;
    int kf6 = 0;
    int kq6 = 0;
    int kp6 = 0;
    int kg6 = 0;
    int kf7 = 0;
    int kq7 = 0;
    int kp7 = 0;
    int kg7 = 0;
    readRegister(ADCR, 0.5);
    readRegister(ADCL, 0.5);
    writeRegister(eqin, 0);
    readRegister(eqin, kg1);
    readRegister(b1b, -kf1);
    readRegister(b1a, 1);
    writeRegister(temp, kq1);
    readRegister(eqin, kg1);
    writeRegister(b1a, 0);
    readRegister(temp, kf1);
    readRegister(b1b, 1);
    writeRegister(b1b, 0);
    readRegister(eqin, 1);
    readRegister(temp, kp1);
    writeRegister(eqin, kg2);
    readRegister(b2b, -kf2);
    readRegister(b2a, 1);
    writeRegister(temp, kq2);
    readRegister(eqin, kg2);
    writeRegister(b2a, 0);
    readRegister(temp, kf2);
    readRegister(b2b, 1);
    writeRegister(b2b, 0);
    readRegister(eqin, 1);
    readRegister(temp, kp2);
    writeRegister(eqin, kg3);
    readRegister(b3b, -kf3);
    readRegister(b3a, 1);
    writeRegister(temp, kq3);
    readRegister(eqin, kg3);
    writeRegister(b3a, 0);
    readRegister(temp, kf3);
    readRegister(b3b, 1);
    writeRegister(b3b, 0);
    readRegister(eqin, 1);
    readRegister(temp, kp3);
    writeRegister(eqin, kg4);
    readRegister(b4b, -kf4);
    readRegister(b4a, 1);
    writeRegister(temp, kq4);
    readRegister(eqin, kg4);
    writeRegister(b4a, 0);
    readRegister(temp, kf4);
    readRegister(b4b, 1);
    writeRegister(b4b, 0);
    readRegister(eqin, 1);
    readRegister(temp, kp4);
    writeRegister(eqin, kg5);
    readRegister(b5b, -kf5);
    readRegister(b5a, 1);
    writeRegister(temp, kq5);
    readRegister(eqin, kg5);
    writeRegister(b5a, 0);
    readRegister(temp, kf5);
    readRegister(b5b, 1);
    writeRegister(b5b, 0);
    readRegister(eqin, 1);
    readRegister(temp, kp5);
    writeRegister(eqin, kg6);
    readRegister(b6b, -kf6);
    readRegister(b6a, 1);
    writeRegister(temp, kq6);
    readRegister(eqin, kg6);
    writeRegister(b6a, 0);
    readRegister(temp, kf6);
    readRegister(b6b, 1);
    writeRegister(b6b, 0);
    readRegister(eqin, 1);
    readRegister(temp, kp6);
    writeRegister(eqin, kg7);
    readRegister(b7b, -kf7);
    readRegister(b7a, 1);
    writeRegister(temp, kq7);
    readRegister(eqin, kg7);
    writeRegister(b7a, 0);
    readRegister(temp, kf7);
    readRegister(b7b, 1);
    writeRegister(b7b, 0);
    readRegister(eqin, 1);
    readRegister(temp, kp7);
    writeRegister(eqin, 1);
    writeDelay("del1", 0, 1);
    readDelay("del1", 0, -1);
    writeDelay("del2", 0, 0);
    readDelay("del1", (int)(del), 1);
    readDelay("del2", (int)(del), 1);
    writeRegister(delout, 0);
    readRegister(lf1a, kfl);
    readRegister(lf1b, 1);
    writeRegister(lf1b, -kfl);
    readRegister(lf1a, kql);
    readRegister(eqin, 0.05);
    writeRegister(lf1a, 0);
    readRegister(lf2a, kfl);
    readRegister(lf2b, 1);
    writeRegister(lf2b, -kfl);
    readRegister(lf2a, kql);
    readRegister(lf1b, 1);
    writeRegister(lf2a, 0);
    readRegister(hf1a, kfh);
    readRegister(hf1b, 1);
    writeRegister(hf1b, 1);
    readRegister(delout, 0.25);
    readRegister(hf1a, kqh);
    writeRegister(delout, 1);
    scaleOffset(-kfh, 0);
    readRegister(hf1a, 1);
    writeRegister(hf1a, 0);
    readRegister(hf2a, kfh);
    readRegister(hf2b, 1);
    writeRegister(hf2b, 1);
    readRegister(delout, 0.3);
    readRegister(hf2a, kqh);
    writeRegister(delout, 1);
    scaleOffset(-kfh, 0);
    readRegister(hf2a, 1);
    writeRegister(hf2a, 0);
    readRegister(lf2b, -2);
    readRegisterFilter(loext, kflext);
    writeRegisterLowshelf(loext, kshext);
    scaleOffset(-2, 0);
    scaleOffset(ampl, 0);
    writeRegister(DACL, 0);
    readRegister(delout, -2);
    scaleOffset(-2, 0);
    scaleOffset(amph, 0);
    writeRegister(DACR, 0);
  }
}
