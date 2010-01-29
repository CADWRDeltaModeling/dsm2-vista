package vista.dsm2;

import java.util.ArrayList;

public class Channel {
	private int number;
	private int index;
	public static int UPSTREAM = 0;
	public static int DOWNSTREAM = 1;
	private float[] bottomElevation;
	private float length;
	private float manning;
	private float dispersion;
	private int [] node;
	private ArrayList<XSection> xsections;
	public Channel(){
		
	}
}
