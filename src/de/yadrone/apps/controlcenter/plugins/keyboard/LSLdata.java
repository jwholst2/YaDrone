package de.yadrone.apps.controlcenter.plugins.keyboard;

import edu.ucsd.sccn.LSL;
import de.yadrone.base.IARDrone;

public class LSLdata implements Runnable {
	
	protected IARDrone drone;	
	private volatile boolean stopRequested = false;
	
	private int screenWidth = 1366;
	private int screenHeight = 768;
	
    private int TobiiChannelCount = 2;
//  private int EEGChannelCount = 2;    
    private LSL.StreamInlet inletET;
    //private LSL.StreamInlet inletEEG;
    
	
	public LSLdata(IARDrone ardrone)  {
		this.drone = ardrone;
		
        System.out.println("Creating Tobii stream inlet...");

        LSL.StreamInfo infoET = new LSL.StreamInfo("Tobii","Gaze",TobiiChannelCount,0,LSL.ChannelFormat.float32,"");
//        LSL.StreamInfo infoEEG = new LSL.StreamInfo("MyStream","Control",EEGChannelCount,20,LSL.ChannelFormat.float32,"");
//        inletEEG = new LSL.StreamInlet(infoEEG);
        inletET = new LSL.StreamInlet(infoET);
		
	}

		public void run() {
		
			while (!stopRequested) {
				
				float[] StreamData = new float[2];
				int numSamples = 30;
				for (int i=0; i<numSamples; i++) {
				    float[] temp_ETsample = new float[TobiiChannelCount];
				//  float[] temp_EEGsample = new float[10*EEGChannelCount];
				    
					try {
						//inletEEG.pull_chunk(temp_EEGsample,EEGtimestamps);
						inletET.pull_sample(temp_ETsample); 
					}
					catch (Exception e) {
						System.out.println("Exception in pull_chunk!!");
					}
					
					// test only EEG or ET at one time
/*	        		StreamData[0] = temp_EEGsample[0]; //left; 0 & 1 values
	        		StreamData[1] = temp_EEGsample[1]; //right; 0 & 1 values */
					StreamData[0] += temp_ETsample[0]; //x-coordinate; pixel number units
					StreamData[1] += temp_ETsample[1]; //y-coordinate; pixel number units
				}
				
				for (int j=0; j<StreamData.length; j++) {
					StreamData[j] /= numSamples;
				}
				processData(StreamData); 
			} 
		}
	   
		public void terminate() {
			stopRequested = true;
		}
		
		// If I want to not process both streams simultaneously, I can send each individual sample here that
		// appends to an array and is only processed once the desired number of samples is collected
		private void processData(float[] data) {
			// assumes top left of screen is (0,0), and width is 2nd value, and down and right is positive
			boolean movement = false;
			if (data[0] < screenWidth/3) {
				drone.spinLeft();
				movement = true;
				System.out.println("looking at left third");
			}
			else if (data[0] > screenWidth*2/3) {
				drone.spinRight();
				movement = true;
				System.out.println("looking at right third");
			}

			if (data[1] < screenHeight/3) {
				drone.up();
				movement = true;
			}
			else if (data[1] > screenHeight*2/3) {
				drone.down();
				movement = true;
			}
			
			if (!movement) {
				drone.hover();
			}
		}
}
