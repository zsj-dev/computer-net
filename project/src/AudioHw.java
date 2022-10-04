import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import com.synthbot.jasiohost.*;

public class AudioHw implements AsioDriverListener {
	private AsioDriver asioDriver;
	private Set<AsioChannel> activeChannels;

	private Queue<Float> recording;
	private Queue<Float> inputrecording;


	private AsioChannel outputChannel;

	private AsioChannel inputChannel;
	private float phase = 0;
	private float phase1 = 0;
	private float freq = 2000f;  // Hz
	private float sampleRate = 48000f;
	private float dphase = (2 * (float)Math.PI * freq) / sampleRate;
	private float dphase_1000 = (2 * (float)Math.PI * 1000f) / sampleRate;
	private float dphase_10000 = (2 * (float)Math.PI * 10000f) / sampleRate;
	
	private float[] output;

	private float[] input;

	//1 means input, -1 means output,0 means input and output
	private int status;


	public void init() {
		activeChannels = new HashSet<AsioChannel>();  // create a Set of AsioChannels

		recording = new LinkedList<>();
		inputrecording = new LinkedList<>();

		if (asioDriver == null) {
			asioDriver = AsioDriver.getDriver("ASIO4ALL v2");
			asioDriver.addAsioDriverListener(this);   // add an AsioDriverListener in order to receive callbacks from the driver

			outputChannel = asioDriver.getChannelOutput(0);

			inputChannel = asioDriver.getChannelInput(0);

			output = new float[Config.HW_BUFFER_SIZE];

			input = new float[Config.HW_BUFFER_SIZE];

			asioDriver.setSampleRate(Config.PHY_TX_SAMPLING_RATE);
			/*
			 * buffer size should be set either by modifying the JAsioHost source code or
			 * configuring the preferred value in ASIO native window. We choose 128 i.e.,
			 * asioDriver.getBufferPreferredSize() should be equal to Config.HW_BUFFER_SIZE
			 * = 128;
			 * 
			 */
			activeChannels.add(inputChannel);
			activeChannels.add(outputChannel);

			asioDriver.createBuffers(activeChannels);  // create the audio buffers and prepare the driver to run
			System.out.println("ASIO buffer created, size: " + asioDriver.getBufferPreferredSize());

		}

	}
	public void insertAudio(){
		float phase = 0;
		for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
			phase = phase + dphase;
			inputrecording.add(phase);  // sine wave
		}
	}




	public void start() {
		if (asioDriver != null) {
			asioDriver.start();  // start the driver
			System.out.println(asioDriver.getCurrentState());
		}
	}

	public void stop() {
		asioDriver.returnToState(AsioDriverState.INITIALIZED);
		asioDriver.shutdownAndUnloadDriver();  // tear everything down
	}

	public void setStatus(int status) {
		this.status = status;
	}

	//pro0 part1
//	@Override
//	public void bufferSwitch(final long systemTime, final long samplePosition, final Set<AsioChannel> channels) {
//		for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
//			output[i]=0.0F;
//		}
//		if (status == 1){
//			for (AsioChannel channelInfo : channels) {
//				if (channelInfo.isInput()){
//					channelInfo.read(input);
//					for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
//						recording.add(input[i]);
//					}
//				}
//
//			}
//
//		}else if(status == -1){
//			for (AsioChannel channelInfo : channels) {
//				if (!(channelInfo.isInput())){
//					for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
//						output[i]=recording.poll();
//					}
//					channelInfo.write(output);
//				}
//			}
//		}else {
//			for (AsioChannel channelInfo : channels) {
//				if (channelInfo.isInput()){
//					channelInfo.read(input);
//					for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
//						recording.add(input[i]);
//					}
//				}else{
//					for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
//						output[i] = inputrecording.poll();
//					}
//					channelInfo.write(output);
//				}
//
//			}
//
//		}
//
//	}

	//pro1 part1
	@Override
	public void bufferSwitch(final long systemTime, final long samplePosition, final Set<AsioChannel> channels) {
		for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
			phase = dphase_1000 + phase;
			phase1 = dphase_10000 + phase1;
			output[i] = (float) (Math.sin((double)phase))+(float)(Math.sin((double)phase1));
		}

		for (AsioChannel channelInfo : channels) {
			if (!(channelInfo.isInput())) {
				channelInfo.write(output);
			}
		}

	}

	@Override
	public void latenciesChanged(final int inputLatency, final int outputLatency) {
		System.out.println("latenciesChanged() callback received.");
	}

	@Override
	public void bufferSizeChanged(final int bufferSize) {
		System.out.println("bufferSizeChanged() callback received.");
	}

	@Override
	public void resetRequest() {
		/*
		 * This thread will attempt to shut down the ASIO driver. However, it will block
		 * on the AsioDriver object at least until the current method has returned.
		 */
		new Thread() {
			@Override
			public void run() {
				System.out.println("resetRequest() callback received. Returning driver to INITIALIZED state.");
				asioDriver.returnToState(AsioDriverState.INITIALIZED);
			}
		}.start();
	}

	@Override
	public void resyncRequest() {
		System.out.println("resyncRequest() callback received.");
	}

	@Override
	public void sampleRateDidChange(final double sampleRate) {
		System.out.println("sampleRateDidChange() callback received.");
	}


}



