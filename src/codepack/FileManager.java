package codepack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author cakeLemon(qwerE)
 */
public class FileManager{
    
    private BpmModifier myGUI;
    private JLabel myText;
    
    private StringBuilder part1 = new StringBuilder();
    private StringBuilder part2 = new StringBuilder();
    
    private File lastFile;
    private File myFile;
    
    public boolean isInt;
    public int bpmValue;
    public float bpmValue_float;
    
    private float actualBpm;
    private double accRatio;
    //private double invRatio;
    
    private int offset;
    private String title;
    
    private boolean hasOriginal;
    private File sav;
    
    public FileManager(BpmModifier myGUI) {
	this.myGUI = myGUI;
	myText = myGUI.getMainText();
        File dat = new File("data" + File.separator + "settings.set");
        boolean error = true;
        if(dat.exists()) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dat), "UTF8"))) {
                String str = br.readLine();
                lastFile = new File(str);
                error = false;
            }
            catch(Exception e) {
                error = true;
            }
        }
        if (error) {
            lastFile = new File(".");
        }
                
    }
    
    public File getLastFile() {
        return lastFile;
    }
    
    public int getSettings(File dir) {
	myFile = dir;
        File dat = new File("data" + File.separator + "settings.set");
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dat), "UTF8"))) {
            bw.write(dir.getParent());
        }
        catch(Exception e) {
            
        }
	
	sav = new File(dir.getPath() + File.separator + "origBpm.sav");
	if(sav.exists()) {
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sav), "UTF8"))){
		Scanner sc = new Scanner(br);
		title = sc.nextLine();
		bpmValue_float = sc.nextFloat();
		if(Math.abs(bpmValue_float - Math.round(bpmValue_float)) < 0.002f) {
		    isInt = true;
		    bpmValue = Math.round(bpmValue_float);
		}
		else {
		    isInt = false;
		}
		offset = sc.nextInt();
		hasOriginal = sc.hasNextInt();
		System.out.println(hasOriginal);
		return 0;
	    }
	    catch(Exception e) {
		    
	    }
	}
	else {
	    hasOriginal = false;
	}
	
        File[] files = dir.listFiles();
        if(files.length == 0) {
            return 1;
        }
        boolean hasKSH = false;
        for(File file : files) {
            if (!file.isDirectory() && file.toString().endsWith(".ksh")) {
                //File file2 = new File(file.getParent() + File.separator + "TEMP_yu481ask90");
                try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
		    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sav), "UTF8"))) {
                    
                    hasKSH = true;
                    String str;
                    int line = 0;

                    while((str = br.readLine()) != null) {
                        line++;
			if(str.startsWith("title=") || line == 1) {
			    title = str;
			    bw.write(title);
			    bw.newLine();
			}
			else if(str.startsWith("t=")) {
                            String[] sp = str.split("[=-]");
                            try {
                                bpmValue_float = Float.parseFloat(sp[1]);
                            }
                            catch(Exception e) {
				e.printStackTrace();
                                String message = "Failed to retrieve BPM value from some .ksh file!";
                                JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
				return 1;
                            }
                            if(Math.abs(bpmValue_float - Math.round(bpmValue_float)) < 0.002f) {
                                isInt = true;
                                bpmValue = Math.round(bpmValue_float);
				bw.write("" + bpmValue);
				bw.newLine();
                                //str = "t=" + bpmValue;
                            }
                            else {
                                isInt = false;
				DecimalFormat fmt = new DecimalFormat("#.###");
				bw.write("" + fmt.format(bpmValue_float));
				bw.newLine();
                                //str = "t=" + bpmValue_float;
                            }
                        }
			else if(str.startsWith("o=")) {
			    String[] sp = str.split("=");
			    try {
                                offset = Integer.parseInt(sp[1]);
				bw.write("" + offset);
				bw.newLine();
                            }
                            catch(Exception e) {
                                String message = "Failed to retrieve offset value from some .ksh file!";
                                JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
				return 1;
                            }
			    break;
			}
                    }
                }
                catch(Exception e) {
                    String message = "Unable to open some .ksh file for reading & writing!";
                    JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
		    return 1;
                }
                break;
            }
        }
	if (hasKSH)
	    return 0;
	else {
	    String message = "The folder does not contain any .ksh files!";
            JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
	    return 1;
	}
    }
    
    
    public int apply(float times, boolean isFloat) {
	
	//myGUI.setMessage("Stretching music... please wait");
	
//	myGUI.originalMsg = myText.getText();
//	myText.setText(myGUI.originalMsg + ": " + "Stretching music... please wait");
//	myGUI.setEnabled(false);
	
	File[] dataFs = new File("data").listFiles();
	for(File file : dataFs) {
	    if(!file.getName().equals("ffmpeg.exe") && !file.getName().equals("settings.set")) {
		try {
		    Files.deleteIfExists(file.toPath());
		}
		catch(Exception e) {
		    
		} 
	    }
	}

	StringBuilder sb;
        File[] files = myFile.listFiles();
        if(files.length == 0) {
            return 1;
        }
        boolean hasKSH = false;
	int count = 0;
	boolean firstFile = true;
	float firstFileBpm = 1;
	double ratio = 1.0;
        for(File file : files) {
	    sb = new StringBuilder();
            if (!file.isDirectory() && file.toString().endsWith(".ksh")) {
		count++;
		
                try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
                    
                    hasKSH = true;
                    String str;
                    int line = 0;
		    boolean firstT = true;
		    float prevBpm = 10;

                    while((str = br.readLine()) != null) {
                        line++;
			if(str.startsWith("title=") || line == 1) {
			    str = title + "(" + Math.round(times) + "%)";
			}
			else if(str.startsWith("t=")) {
			    String[] sp = str.split("[=-]");
			    if(firstT) {
				firstT = false;
				prevBpm = Float.parseFloat(sp[1]);
				if(firstFile) {
				    firstFileBpm = prevBpm;
				}
				if(firstFile || firstFileBpm == prevBpm) {
				    if (isFloat) {
					DecimalFormat fmt = new DecimalFormat("#.###");
					str = "t=" + fmt.format(bpmValue_float * times / 100f);
					actualBpm = Float.parseFloat(fmt.format(bpmValue_float * times / 100f));
				    }
				    else {
					str = "t=" + (int)(bpmValue_float * times / 100f);
					actualBpm = (int)(bpmValue_float * times / 100f);
				    }
				    ratio = (double)actualBpm / prevBpm;
				}
				else {
				    DecimalFormat fmt = new DecimalFormat("#.###");
				    str = "t=" + fmt.format(prevBpm * ratio);
				}
				if(sp.length >= 3) {
				    str += "-" + (float)(Float.parseFloat(sp[2]) * ratio);
				}
			    }
			    else {
				str = "t=" + (float)(Float.parseFloat(sp[1]) * ratio);
			    }
                        }
			else if(str.startsWith("o=")) {
			    String[] sp = str.split("=");
			    accRatio = (double)actualBpm / bpmValue_float;
			    //erase this
			    System.out.println(accRatio);
			    //invRatio = (double)bpmValue_float / actualBpm;
			    
			    str = "o=" + (int)Math.round((double)Integer.parseInt(sp[1]) / ratio);
			}
                        sb.append(str);
                        sb.append(System.getProperty("line.separator"));
                    }
                }
                catch(Exception e) {
                    String message = "Unable to open some .ksh file for reading & writing!";
                    JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
		    return 1;
                }
		
		String hashStr = "" + ((Float)times).hashCode();
		if(isFloat) {
		    hashStr += "f";
		}
		File fileW = new File(file.getParent() + File.separator + count + "_" + hashStr + ".ksh");
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileW), "UTF8"))) {
		    bw.append(sb);
		    Files.deleteIfExists(file.toPath());
		}
		catch(Exception e) {
		    String message = "Unable to open some .ksh file for reading & writing!";
                    JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
		    return 1;
		}
            }
        }
	if (!hasKSH) {
	    String message = "The folder does not contain any .ksh files!";
            JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
	    return 1;
	}
	
	//Stretch music
	boolean hasMusic = false;
	String ext;
	Path data = new File("data").toPath();
	for(File file : files) {
	    if(!file.isDirectory() && (file.toString().endsWith(".mp3") || file.toString().endsWith(".ogg") || file.toString().endsWith(".wav"))) {
		if(!hasOriginal) {
		    try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sav, true), "UTF8"))) {
			bw.write("1");
			bw.newLine();
			Path dst = new File(file.getParent() + File.separator + "Orig___" + file.getName()).toPath();
			Files.copy(file.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);
		    }
		    catch(Exception e) {
			String message = "Failed to write files to the savefile!";
			JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
			return 1;
		    }
		}
		else {
		    if(!file.getName().startsWith("Orig___"))
			continue;
		}
		
		hasMusic = true;
		if (file.toString().endsWith(".mp3"))
		    ext = ".mp3";
		else if (file.toString().endsWith(".ogg"))
		    ext = ".ogg";
		else
		    ext = ".wav";
	    }
	    else 
		continue;
	    
	    try{
		Path dst = new File(data + File.separator + "tempMusic" + ext).toPath();
		Files.copy(file.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);
	    }
	    catch(Exception e) {
		String message = "Failed to copy files from the folder!";
		JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
		return 1;
	    }
	    
	    try{
		ProcessBuilder pb;
		pb = new ProcessBuilder("data" + File.separator + "ffmpeg.exe",
					"-i", 
					"tempMusic" + ext,
					"-filter:a", 
					"\"atempo=" + accRatio + "\"", 
					"-vn",
					file.getName());
		//pb.redirectOutput(new File("ERR"));
		pb.directory(new File("data"));
		Process p = pb.start();
                long startTime = System.nanoTime();
		final long sec = 1000000000L;
		
		while(!p.waitFor(10, TimeUnit.MILLISECONDS)) {
		}
		
	    }
	    catch(Exception e) {
		String message = "Failed to stretch music!";
		JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
		return 1;
	    }
	    
	    try {
		Path src = new File("data" + File.separator + file.getName()).toPath();
		if (!hasOriginal)
		    Files.copy(src, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		else {
		    Path dst = new File(file.getParent() + File.separator + file.getName().substring(7)).toPath();
		    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
		}
		Files.deleteIfExists(src);
	    }
	    catch(Exception e) {
		String message = "Failed to copy/delete files from the folder!";
		JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
		return 1;
	    }
	    
	}
	if(!hasMusic) {
	    String message = "The folder does not contain any .mp3 or .ogg files!";
            JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
	    return 1;
	}
	
	hasOriginal = true;
	myGUI.eraseMessage();
	
	return 0;
    }
}
//TODO Change rate to actual BPM/BPM double. take offset into consideration.