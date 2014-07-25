import org.andrewkilpatrick.elmGen.ElmProgram;
// ;sample reverb program for FV-1
// ;minimize number of delays and ops.
// ;4 aps driving 2 AP-delay loops
// ;drive both loop elements, take output from each
// ;no pot controls
// ;output is full reverb, not mixed
// ;22 operations (of 128)
// 
// mem	api1	122
// mem	api2	303
// mem	api3	553
// mem	api4	922
// 
// mem	ap1	3823
// mem	del1	8500	;input = left output
// 
// mem	ap2	4732
// mem	del2	7234	;input = right output
// 
// equ	krt	0.7	;adjust reverb time
// equ	kap	0.625	;adjust AP coefficients
// equ	apout	reg0	;holding reg input AP signal
// 
// ;input all passes (2)
// 
// rdax	adcl,0.25	;read inputs,
// rdax	adcr,0.25	;attenuate, sum and
// rda	api1#,kap	;do 4 APs
// wrap	api1,-kap
// rda	api2#,kap
// wrap	api2,-kap
// rda	api3#,kap
// wrap	api3,-kap
// rda	api4#,kap
// wrap	api4,-kap
// wrax	apout,1		;write to min, keep in ACC
// 
// ;first loop apd:
// ;AP'd input in ACC
// rda	del2#,krt	;read del2, scale by Krt
// rda	ap1#,-kap	;do loop ap
// wrap	ap1,kap
// wra	del1,1.99	;write delay, x2 for dac out
// wrax	dacl,0
// 
// ;second loop apd:
// 
// rdax	apout,1		;get input signal again
// rda	del1#,krt	;as above, to other side of loop
// rda	ap2#,kap
// wrap	ap2,-kap
// wra	del2,1.99
// wrax	dacr,0
// 
// 
public class MinRev1 extends ElmProgram {
  public MinRev1() {
    super("MinRev1");
    setSamplerate(48000);
    allocDelayMem("api1", 122);
    allocDelayMem("api2", 303);
    allocDelayMem("api3", 553);
    allocDelayMem("api4", 922);
    allocDelayMem("ap1", 3823);
    allocDelayMem("del1", 8500);
    allocDelayMem("ap2", 4732);
    allocDelayMem("del2", 7234);
    double krt = 0.7;
    double kap = 0.625;
    int apout = REG0;
    readRegister(ADCL, 0.25);
    readRegister(ADCR, 0.25);
    readDelay("api1", 1.0, kap);
    writeAllpass("api1", 0, -kap);
    readDelay("api2", 1.0, kap);
    writeAllpass("api2", 0, -kap);
    readDelay("api3", 1.0, kap);
    writeAllpass("api3", 0, -kap);
    readDelay("api4", 1.0, kap);
    writeAllpass("api4", 0, -kap);
    writeRegister(apout, 1);
    readDelay("del2", 1.0, krt);
    readDelay("ap1", 1.0, -kap);
    writeAllpass("ap1", 0, kap);
    writeDelay("del1", 0, 1.99);
    writeRegister(DACL, 0);
    readRegister(apout, 1);
    readDelay("del1", 1.0, krt);
    readDelay("ap2", 1.0, kap);
    writeAllpass("ap2", 0, -kap);
    writeDelay("del2", 0, 1.99);
    writeRegister(DACR, 0);
  }
}
