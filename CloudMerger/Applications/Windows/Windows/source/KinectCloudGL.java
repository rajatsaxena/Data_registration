import processing.core.*; 
import processing.xml.*; 

import processing.opengl.*; 
import javax.media.opengl.*; 
import javax.media.opengl.glu.*; 
import java.nio.ByteBuffer; 
import java.nio.ByteOrder; 
import java.nio.FloatBuffer; 
import peasy.*; 
import javax.swing.*; 
import java.text.DecimalFormat; 
import Jama.Matrix; 
import Jama.EigenvalueDecomposition; 

import Jama.util.*; 
import Jama.*; 
import Jama.test.*; 
import Jama.examples.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class KinectCloudGL extends PApplet {








 




/*
Object declarations:
*/
int currentScreen; //this decides which screen is being drawn
PeasyCam camera1; //Camera object, allows the user to navigate around the cloud using mouse
Button laodcloud1button, laodcloud2button, viewcloud1button, viewcloud2button, 
  transformCloudButton, viewBigCloudButton, resetRegistrationButton, printFileButton; 
boolean cameraOn = false; //camera must be off when displaying menu screen


GLU glu; //OpenGL objects
GL gl, gl2;

float[] cloud1, cloud2;
int[] cloud1col;
String cloud1filename;
String cloud2filename;
String cloud2abspath;

float[] transCloud = null;
float[] bigCloud = null;
PVector[] vectorCloud1;
PVector[] vectorCloud2;
ArrayList cloud1PointsList = new ArrayList();//Lists for holding corresponding points?
ArrayList cloud2PointsList = new ArrayList();

float pointSize = 5f; // set the point size of the point cloud
FloatBuffer f1, f2, f3;

NumberFormat formatter = new DecimalFormat("#0.000"); //Tidying up numbers for file writing

/*
setup() is called when application is launched
*/
public void setup(){
  //set up application screen size and renderer
  if(screen.width>1300){
    size(800, 600, OPENGL);
  }
  else{
    size(640, 480, OPENGL);
  }
  /*
  Object instantiators:
  */
  laodcloud1button = new Button(width/2-Math.round(textWidth("Load cloud 1"))-50, 50, "Load cloud 1");
  laodcloud2button = new Button(width/2+30, 50, "Load cloud 2");
  viewcloud1button = new Button(width/2-Math.round(textWidth("View cloud 1"))-50, 100, "View cloud 1");
  viewcloud2button = new Button(width/2+30, 100, "View cloud 2");
  transformCloudButton = new Button(width/2-Math.round(textWidth("Merge clouds")/2), 200, "Merge clouds");
  viewBigCloudButton = new Button(width/2-Math.round(textWidth("View merge clouds")/2), 250, "View merged clouds");
  printFileButton = new Button(width/2-Math.round(textWidth("Print merged cloud to file")/2), 300, "Print merged cloud to file");
  resetRegistrationButton = new Button(width/2-Math.round(textWidth("Reset registration")/2), 400, "Reset registration");
 
////////For very accurate merging of test data,
////////Uncomment the following to start application with :
// cloud1 = loadPoints("pcKinect_reference.txt");
// vectorCloud1 = loadVectors("pcKinect_reference.txt");
// cloud2 = loadPoints("pcKinect_transformed.txt");
// vectorCloud2 = loadVectors("pcKinect_transformed.txt");
// f1 = loadFloats(cloud1);
// f2 = loadFloats(cloud2);
// cloud1PointsList.add(new PVector(172.12, 312.35, 129.05));
// cloud1PointsList.add(new PVector(52.372, 162.82, -66.771));
// cloud1PointsList.add(new PVector(-154.84, 339.61, -139.42));
// cloud2PointsList.add(new PVector(280.63, 220.26, -34.754));
// cloud2PointsList.add(new PVector(54.642, 131.86, -161.87));
// cloud2PointsList.add(new PVector(-71.122, 379.58, -209.71));
  
}

/*
draw() is called every time the screen is updated, 60 times per second
*/
public void draw(){
  background(0);
  switch(currentScreen) {
    case 0: drawOpenScreen(); break;
    case 1: drawCloudOne(); break;
    case 2: drawCloudTwo(); break;
    case 3: drawBigCloud(); break;
    default: drawOpenScreen(); break;
  }
  shouldCameraBeOn();
}

class Button{
  int x,y;
  String label;
  
  Button(int x, int y, String label){
    this.x = x;
    this.y = y;
    this.label = label;
  }
  public void draw(){
    fill(200);
    if(over()){
	fill(255);
    }
    rect(x, y, textWidth(label)+20, 20);
    fill(0);
    text(label, x+10, y + 15);
  }
  public boolean over(){
    if(mouseX >= x && mouseY >= y && mouseX <= x + textWidth(label)+20 && mouseY <= y + 22){
	return true;
    }
    return false;
  }
}
//
public FloatBuffer loadFloats(float[] points) {
  FloatBuffer f;
  f = ByteBuffer.allocateDirect(4 * points.length).order(
  ByteOrder.nativeOrder()).asFloatBuffer();
  f.put(points);
  f.rewind();
  return f;
}

//Defines how to read the file with the points
public float[] loadPoints(String path) {
  String[] raw = loadStrings(path);
  float[] points = new float[raw.length * 3];
  //colors = new float[raw.length*4];
  for (int i = 0; i < raw.length; i++) {
    String[] thisLine = split(raw[i], ' ');
    points[i * 3] = new Float(thisLine[0]).floatValue();
    points[i * 3 + 1] = new Float(thisLine[1]).floatValue();
    points[i * 3 + 2] = new Float(thisLine[2]).floatValue();
  }
  return points;
}

//Same as loadPoints, but reads colors in stead, needed for writing the transformed cloud
public int[] loadColors(String path) {
  String[] raw = loadStrings(path);
  int[] col = new int[raw.length * 3];
  //colors = new float[raw.length*4];
  for (int i = 0; i < raw.length; i++) {
    String[] thisLine = split(raw[i], ' ');
    col[i * 3] = Integer.parseInt(thisLine[3]);
    col[i * 3 + 1] = Integer.parseInt(thisLine[4]);
    col[i * 3 + 2] = Integer.parseInt(thisLine[5]);
  }
  return col;
}


//Array of PVectors from file
public PVector[] loadVectors(String path) {
  String[] raw = loadStrings(path);
  PVector[] vectors = new PVector[raw.length];
  //colors = new float[raw.length*4];
  for (int i = 0; i < raw.length; i++) {
    String[] thisLine = split(raw[i], ' ');
    vectors[i] = new PVector((new Float(thisLine[0]).floatValue()),(new Float(thisLine[1]).floatValue()),(new Float(thisLine[2]).floatValue()));
  }
  return vectors;
}

/*
unProject() and project() methods adapted from Nathan Nifong's 3d point manipulation project
http://nathannifong.com/3d_point_manipulation/_3D_Point_Manipulation.pde
*/
public PVector unProject(float winX, float winY)
{
  GL gl=((PGraphicsOpenGL)g).gl;
  GLU glu=((PGraphicsOpenGL)g).glu;
  ((PGraphicsOpenGL)g).beginGL();
  int viewport[] = new int[4];
  double[] proj=new double[16];
  double[] model=new double[16];
  gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
  gl.glGetDoublev(GL.GL_PROJECTION_MATRIX,proj,0);
  gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX,model,0);
  FloatBuffer fb = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
  gl.glReadPixels(PApplet.parseInt(winX), PApplet.parseInt(height-winY), 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, fb);
  fb.rewind(); 
  double[] mousePosArr=new double[4];
  glu.gluUnProject((double)mouseX,height-(double)mouseY,(double)fb.get(0),model,0,proj,0,viewport,0,mousePosArr,0);
  ((PGraphicsOpenGL)g).endGL();
  return new PVector((float)mousePosArr[0],(float)mousePosArr[1],(float)mousePosArr[2]);
}

public PVector project(PVector obj)
{
  // same as above, but projecting instead of unprojecting
  GL gl=((PGraphicsOpenGL)g).gl;
  GLU glu=((PGraphicsOpenGL)g).glu;
  ((PGraphicsOpenGL)g).beginGL();
  int viewport[] = new int[4];
  double[] proj=new double[16];
  double[] model=new double[16];
  gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
  gl.glGetDoublev(GL.GL_PROJECTION_MATRIX,proj,0);
  gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX,model,0);
  double[] winPosArr=new double[3];
  glu.gluProject(obj.x, obj.y, obj.z, model, 0, proj, 0, viewport, 0, winPosArr, 0);
  ((PGraphicsOpenGL)g).endGL();
  
  return new PVector((float)winPosArr[0],(float)winPosArr[1],(float)winPosArr[2]);
}

/*
Hardcoded choosefile methods. should be merged.
*/
File dir = new File("");
public void chooseFile1(){
  SwingUtilities.invokeLater(new Runnable() {
    public void run() {
      try {
        try { 
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } 
        catch (Exception e) { 
          e.printStackTrace();  
        }
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(dir);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          cloud1filename = file.getName();
          println(cloud1filename);
          dir = new File(file.getAbsolutePath());
          cloud1 = loadPoints(file.getAbsolutePath());
          cloud1col = loadColors(file.getAbsolutePath());
          vectorCloud1 = loadVectors(file.getAbsolutePath());
          f1 = loadFloats(cloud1);
        } 
        else {
          println("Open command cancelled by user.");
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  });
}
public void chooseFile2(){
  SwingUtilities.invokeLater(new Runnable() {
    public void run() {
      try {
        try { 
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } 
        catch (Exception e) { 
          e.printStackTrace();  
        } 
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(dir);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          cloud2filename = file.getName();
          cloud2abspath = file.getAbsolutePath();
          dir = new File(file.getAbsolutePath());
          cloud2 = loadPoints(file.getAbsolutePath());
          vectorCloud2 = loadVectors(file.getAbsolutePath());
          f2 = loadFloats(cloud2);
        } 
        else {
          println("Open command cancelled by user.");
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  });
}
/*
Allow user to reset registration if mistake is made.
*/
public void resetPointLists(){
  cloud1PointsList.clear();
  cloud2PointsList.clear();
}
public void writeFiles(){
  /*
  Writing report file
  */
  String filename = selectOutput();
  println("Writing report");
  PrintWriter report;
  report = createWriter(filename+"_report.txt");
  
  report.println("Merged clouds " + cloud1filename + " and " + cloud2filename);
  report.println();report.println();
  report.println("Selected points in " + cloud1filename + ":");
  for(int i=0;i<cloud1PointsList.size(); i++){
    report.println(cloud1PointsList.get(i));
  }
  report.println();
  report.println("Selected points in " + cloud2filename + ":");
  for(int i=0;i<cloud2PointsList.size(); i++){
    report.println(cloud2PointsList.get(i));
  }
  report.println();
  report.println("Rotation matrix:");
  for(int i=0; i<R.length; i++){
    report.println(formatter.format(R[i][0]) + ", " + formatter.format(R[i][1]) + ", " + formatter.format(R[i][2]));
  }
  report.println();
  report.println("Translation parameters:");
  report.println("X: " + formatter.format(T[0]) + ", Y: " + formatter.format(T[1]) + ", Z: " + formatter.format(T[2]));
  report.println();
  report.println("Diff matrix:");
  for(int i=0; i<diff.length; i++){
    report.println(formatter.format(diff[i][0]) + ", " + formatter.format(diff[i][1]) + ", " + formatter.format(diff[i][2]));
  }
  report.println();
  report.println("Residuals:");
  report.println("X: " + formatter.format(res[0]) + ", Y: " + formatter.format(res[1]) + ", Z: " + formatter.format(res[2]));
  report.flush();
  report.close();
  
  /*
  Writing merged pointcloud
  */
  println("writing output cloud");
  PrintWriter output;
  output = createWriter(filename+".txt");
  //Start by copying cloud 2 to the new file
  String[] raw = loadStrings(cloud2abspath);
  for (int i = 0; i < raw.length; i++) {
    output.println(raw[i]);
  }
  //Then write transformed cloud:
  for(int i=0; i<cloud1.length/3;i++){
    output.println(formatter.format(f3.get(i*3+0)) + " " + formatter.format(f3.get(i*3+1)) + " " + formatter.format(f3.get(i*3+2)) + " " +
      cloud1col[i*3+0] + " " + cloud1col[i*3+1] + " " + cloud1col[i*3+2]);
  }
  
  output.flush();
  output.close();
}
/*
Methods to handle key press actions
*/

public void keyTyped(){
  if(key == TAB){
    if((cloud1!=null)&&(cloud2!=null)){
      currentScreen++;
      if (currentScreen > 2) { 
        currentScreen = 0; 
      }
      shouldCameraBeOn();
      println("Current screen is "+currentScreen);
    }
  }
  if(key == 'l'){ //print a list of the selected points
    if(cloud1PointsList!=null){
      println("Selected points in cloud 1:");
      for(int i=0; i<cloud1PointsList.size(); i++){
        println(cloud1PointsList.get(i));   
      }
    }
    if(cloud2PointsList!=null){
      println("Selected points in cloud 2:");
      for(int i=0; i<cloud2PointsList.size(); i++){
        println(cloud2PointsList.get(i));   
      }
    }
  }
  if(key == BACKSPACE){
    currentScreen=0;
    shouldCameraBeOn();
  }

}
/*
Methods to handle mouse actions
*/

public void mouseClicked(){
  if(currentScreen == 0){
      if(laodcloud1button.over()){
        println("load cloud 1");
        chooseFile1();
      }
      if(laodcloud2button.over()){
        println("load cloud 2");
        chooseFile2();
      }
      if(viewcloud1button.over()){
        if(cloud1!=null){
          currentScreen = 1;
          shouldCameraBeOn();
        }
        else{
          println("load a cloud first");
        }
      }
      if(viewcloud2button.over()){
        if(cloud2!=null){
          currentScreen = 2;
          shouldCameraBeOn();
        }
        else{
          println("load a cloud first");
        }
      }
      if(transformCloudButton.over()){
        getRotationAndTranslation(cloud1PointsList, cloud2PointsList);
        transformateCloud(cloud1,R,T);
      }
      if(viewBigCloudButton.over()){
        currentScreen = 3;
        shouldCameraBeOn();
      }
      if(printFileButton.over()){
        writeFiles();
      }
      if(resetRegistrationButton.over()){
        resetPointLists();
      }
    }
    if(keyPressed){
      if(keyCode == SHIFT){
        if(currentScreen==1){
          println("Clicked point is: " + (unProject(mouseX,mouseY)));
          if(selectClosestPoint((unProject(mouseX,mouseY)),(vectorCloud1))!=null){
            cloud1PointsList.add(selectClosestPoint((unProject(mouseX,mouseY)),(vectorCloud1)));
          }
          println("number of selected points in cloud 1: "+cloud1PointsList.size());
        }
        if(currentScreen==2){
          println("Clicked point is: " + (unProject(mouseX,mouseY)));
          if(selectClosestPoint((unProject(mouseX,mouseY)),(vectorCloud2))!=null){
            cloud2PointsList.add(selectClosestPoint((unProject(mouseX,mouseY)),(vectorCloud2)));
          }
        println("number of selected points in cloud 2: "+cloud2PointsList.size());

        }
      }
    }  

  
}

public PVector selectClosestPoint(PVector mouseClick, PVector[] cloud){
  float closeDist = 9999.9999f;
  PVector closestPoint = null;
  for(int i = 0; i<cloud.length; i++){
    if(mouseClick.dist(cloud[i])<closeDist){
      if(mouseClick.dist(cloud[i])<100){
        closestPoint = cloud[i];
      }
      closeDist = mouseClick.dist(cloud[i]);
    }
  }
  println("Closest point is: " + closestPoint);
  println("Distance: " + closeDist);
  return closestPoint;
}
PVector meanset1;
PVector meanset2;
double[][] pntSet1c;
double[][] pntSet2c;
double[][] CCp1p2;
double[][] A;
double[] delta;
double[][] Q;
Matrix Qmat;
double[][] eigenvalue;
double[][] eigenvector;
double[] qR;
double[][] R; //the rotation matrix
double[] T; // the translation parameters
double[][] diff;
double[] res; //residuals

/* Acquire rotation and translation matrices from selected corresponding points.
Based on MATLAB script by K. Khoshelham, July 2010
Author: John Wika Haakseth, May 2012
*/
public void getRotationAndTranslation(ArrayList<PVector> pointset1, ArrayList<PVector> pointset2){
  //Checks to avoid executing method if pointsets are of uneven size or they have less than three points:
  if(pointset1.size() != pointset2.size()){
    println("pointlists need to be of same size: Set 1: " + pointset1.size() + ", Set 2: " + pointset2.size());
    return;
  }
  if(pointset1.size() < 3){
    println("pointlists need to be of size 3 or larger: Set 1: " + pointset1.size() + ", Set 2: " + pointset2.size());
    return;
  }
  //for some operations it's handy to have the pointsets as lists of doubles
  double[][] pointset1db = new double[pointset1.size()][3];
  for(int i=0;i<pointset1.size();i++){
    pointset1db[i][0]=pointset1.get(i).x;pointset1db[i][1]=pointset1.get(i).y;pointset1db[i][2]=pointset1.get(i).z;
  }
  double[][] pointset2db = new double[pointset2.size()][3];
  for(int i=0;i<pointset2.size();i++){
    pointset2db[i][0]=pointset2.get(i).x;pointset2db[i][1]=pointset2.get(i).y;pointset2db[i][2]=pointset2.get(i).z;
  }
  
  int n = pointset1.size();
  int x1=0, y1=0, z1=0, x2=0, y2=0, z2=0;
  for(int i=0; i<pointset1.size();i++){
    x1+=pointset1.get(i).x;
    y1+=pointset1.get(i).y;
    z1+=pointset1.get(i).z;
    x2+=pointset2.get(i).x;
    y2+=pointset2.get(i).y;
    z2+=pointset2.get(i).z;
  }
  meanset1 = new PVector(x1/n,y1/n,z1/n);
  meanset2 = new PVector(x2/n,y2/n,z2/n);
  
  //mean centered points
  pntSet1c = new double[n][3];
  pntSet2c = new double[n][3];
  
  for(int i=0; i<n; i++){
    for(int j=0; j<3; j++){
      switch(j) {
      case 0: 
        pntSet1c[i][j] = pointset1.get(i).x-meanset1.x; 
        pntSet2c[i][j] = pointset2.get(i).x-meanset2.x; 
        break;
      case 1: 
        pntSet1c[i][j] = pointset1.get(i).y-meanset1.y; 
        pntSet2c[i][j] = pointset2.get(i).y-meanset2.y; 
        break;
      case 2: 
        pntSet1c[i][j] = pointset1.get(i).z-meanset1.z; 
        pntSet2c[i][j] = pointset2.get(i).z-meanset2.z; 
        break;
      }
    }
  }
  
  CCp1p2 = new double[3][3];
  for(int i=0; i<3; i++){
    for(int j=0; j<3; j++){
      for(int k=0; k<n; k++){
        CCp1p2[i][j] += pntSet1c[k][i] * pntSet2c[k][j] / n;
      }
    }
  }
  println();
  println("CCp1p2:");
  for(int i=0; i<3; i++){
    println(CCp1p2[i][0] + ", " + CCp1p2[i][1] + ", " + CCp1p2[i][2]);
  }
  
  //A = CCp1p2 - CCp1p2';
  A = new double[3][3];
  for(int i=0; i<3; i++){
    for(int j=0;j<3; j++){
      A[i][j] = CCp1p2[i][j] - CCp1p2[j][i];
    }
  }
  
  println();
  println("A:");
  for(int i=0; i<3; i++){
    println(A[i][0] + ", " + A[i][1] + ", " + A[i][2]);
  }
  
  delta = new double[]{A[1][2], A[2][0], A[0][1]}; 
  println();
  println("delta: " + delta[0] + ", " +delta[1] + ", " +delta[2]);
  
  double CCtrace = 0;
  for(int i=0; i<CCp1p2.length; i++){
    CCtrace += CCp1p2[i][i];
  }
  
  println();
  println("trace: " + CCtrace);
  
  Q = new double[4][4];
  //Make first row and column of Q
  Q[0][0] = CCtrace; //Top left item is trace
  for(int i=1; i<Q.length; i++){
    Q[0][i] = delta[i-1];
  }
  for(int i=1; i<Q.length; i++){
    Q[i][0] = delta[i-1];
  }
  //CCp1p2+CCp1p2'-trace(CCp1p2)*eye(3)
  for(int i=1; i<Q.length; i++){
    for(int j=1;j<Q.length; j++){
      Q[i][j] = CCp1p2[i-1][j-1] + CCp1p2[j-1][i-1];
    }
  }
  for(int i=1; i<Q.length; i++){//-trace(CCp1p2)*eye(3)
    Q[i][i] -= CCtrace;
  }
  
  println();
  println("Q:");
  for(int i=0; i<Q.length; i++){
    println(Q[i][0] + ", " + Q[i][1] + ", " + Q[i][2] + ", " + Q[i][3]);
  }

  Qmat = new Matrix(Q);
//  println(Qmat.)
  EigenvalueDecomposition eigdec = Qmat.eig();
  Matrix Qeigval = eigdec.getD(); // Returns the block diagonal eigenvalue matrix
  Matrix Qeigvec = eigdec.getV(); // Returns the eigenvector matrix
  
  println();
  println("Eigenvalue:");
  for(int i=0; i<Qeigval.getRowDimension(); i++){
    println(Qeigval.get(i,0) + ", " + Qeigval.get(i,1) + ", " + Qeigval.get(i,2) + ", " + Qeigval.get(i,3));
  }
  
  // NEGATIVE VALUES COMPARED TO MATLAB SCRIPT
  println();
  println("Eigenvector:");
  for(int i=0; i<Qeigvec.getRowDimension(); i++){
    println(Qeigvec.get(i,0) + ", " + Qeigvec.get(i,1) + ", " + Qeigvec.get(i,2) + ", " + Qeigvec.get(i,3));
  }
  
  double val = Qeigval.get(0,0);
  int valcolumn = 0;
  for(int i=0; i<Qeigval.getColumnDimension(); i++){
    if(Qeigval.get(i,i)>val){
      val = Qeigval.get(i,i);
      valcolumn =i;
    }
  }
  println();
  println("val = " + val + ", index " + valcolumn);
  println();
  println("qR:");
  
  qR = new double[4];
  for(int i=0; i<qR.length; i++){
    qR[i] = Qeigvec.get(i,valcolumn);
    println(qR[i]);
  }
  
  //qR2R, make the rotation matrix
  double q0 = qR[0], q1 = qR[1], q2 = qR[2], q3 = qR[3];
  R = new double[][]{
    {(q0*q0+q1*q1-q2*q2-q3*q3),(2*(q1*q2-q0*q3)),(2*(q1*q3+q0*q2))},
    {(2*(q1*q2+q0*q3)),(q0*q0+q2*q2-q1*q1-q3*q3),(2*(q2*q3-q0*q1))},
    {(2*(q1*q3-q0*q2)),(2*(q2*q3+q1*q0)),(q0*q0+q3*q3-q1*q1-q2*q2)}
  };
  
  println();
  println("R:");
  for(int i=0; i<R.length; i++){
    println(R[i][0] + ", " + R[i][1] + ", " + R[i][2]);
  }
  

  //translation matrix, meanset2'-R*meanset1'
  T = new double[]{
    (meanset2.x-((R[0][0]*meanset1.x)+(R[0][1]*meanset1.y)+(R[0][2]*meanset1.z))),
    (meanset2.y-((R[1][0]*meanset1.x)+(R[1][1]*meanset1.y)+(R[1][2]*meanset1.z))),
    (meanset2.z-((R[2][0]*meanset1.x)+(R[2][1]*meanset1.y)+(R[2][2]*meanset1.z)))
  };
  
  println();
  println("T:");
  println(T[0] + ", " + T[1] + ", " + T[2]);

  //calculate residuals
  diff = new double[3][3]; //diff = pntSet2' - R*pntSet1' - repmat(T,1,nPnts1);
  //pntSet2'
  for(int i=0; i<3; i++){
    for(int j=0;j<3;j++){
      switch(j) {
      case 0: 
        diff[j][i] = pointset2.get(i).x; 
        break;
      case 1: 
        diff[j][i] = pointset2.get(i).y; 
        break;
      case 2: 
        diff[j][i] = pointset2.get(i).z; 
        break;
      }
    }
  }
  //(-)R*pntSet1
  for(int i=0; i<3; i++){
    for(int j=0; j<3; j++){
      for(int k=0; k<3; k++){
        diff[i][j] -= R[i][k] * pointset1db[j][k];
      }
    }
  }
  //(-) repmat(T,1,nPnts1)
  for(int i=0; i<3;i++){
    diff[0][i] -= T[0];
    diff[1][i] -= T[1];
    diff[2][i] -= T[2];
  }
  
  println();
  println("diff:");
  for(int i=0; i<diff.length; i++){
    println(diff[i][0] + ", " + diff[i][1] + ", " + diff[i][2]);
  }
  
  res = new double[3];
  res[0]=Math.sqrt((diff[0][0]*diff[0][0])+(diff[1][0]*diff[1][0])+(diff[2][0]*diff[2][0]));
  res[1]=Math.sqrt((diff[0][1]*diff[0][1])+(diff[1][1]*diff[1][1])+(diff[2][1]*diff[2][1]));
  res[2]=Math.sqrt((diff[0][2]*diff[0][2])+(diff[1][2]*diff[1][2])+(diff[2][2]*diff[2][2]));
  
  println();
  println("res:");
  for(int i=0; i<3; i++){
    println(res[i]);
  }
}


/**The transformation: 
- Takes in cloud array n*3x1, rotation matrix and translation matrix.
- Converts cloud array to nx3 matrix (n points, 3 dimensions)
- Applies rotation matrix. Matrix multiplication between R and cloud
- Adds on the translation parameters.

- Print to file?
- Merge with 2nd cloud?
- Display resulting, larger cloud?
Author: John Wika Haakseth, May 2012
**/
public void transformateCloud(float[] cloud, double[][] R, double[] T){
  double[][] cloud_trans = new double[cloud.length/3][3];
  for(int i=0; i<cloud_trans.length;i++){
    cloud_trans[i][0] = cloud[3*i+0];
    cloud_trans[i][1] = cloud[3*i+1];
    cloud_trans[i][2] = cloud[3*i+2];
  }
  //Rotation, R*cloud_trans:
  double[][] rotated = new double[cloud_trans.length][3];
  for(int i=0; i<rotated.length; i++){
    for(int j=0; j<3; j++){
      for(int k=0; k<3; k++){
        rotated[i][j] += R[j][k] * cloud_trans[i][k];
      }
    }
  }
  
  //Translation, rotated + T;
  for(int i=0;i<rotated.length;i++){
    rotated[i][0] = rotated[i][0] + T[0];
    rotated[i][1] = rotated[i][1] + T[1];
    rotated[i][2] = rotated[i][2] + T[2];
  }
  println();
  println(rotated.length);
  println("The last point: " + rotated[rotated.length-1][0] +", "+ rotated[rotated.length-1][1] +", "+rotated[rotated.length-1][2]);
  
  transCloud = new float[cloud1.length];
  //put transformed cloud into float array
  for(int i=0; i<rotated.length-1;i++){
    for(int j=0; j<3; j++){
      transCloud[i*3+j] = (float)rotated[i][j];
    }
  }
  f3 = loadFloats(transCloud);
}

/*
The methods for drawing the different screens.
*/

//Screen that shows when application is launched, user clicks a button to open filechoosers to find clouds.
public void drawOpenScreen(){
  
  laodcloud1button.draw();
  laodcloud2button.draw();
  viewcloud1button.draw();
  viewcloud2button.draw();
  if(cloud1PointsList.size()>2 && cloud2PointsList.size()>2){
    transformCloudButton.draw();
  }
  if(transCloud!=null){
    viewBigCloudButton.draw();
    printFileButton.draw();
  }
  if(cloud1PointsList.size()!=0 || cloud2PointsList.size()!=0){
    resetRegistrationButton.draw();
  }
  
}

public void drawCloudOne(){
  //Get GL object to make direct OpenGL calls
  PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
  gl = pgl.beginGL();
  gl.glPointSize(pointSize); //default is 1
  gl.glColor4f(0.6f,0.5f,0.6f,0.6f);   
  gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
  gl.glVertexPointer(3, GL.GL_FLOAT, 0, f1); //define an array of vertex data, gets points from FloatBuffer f
  gl.glDrawArrays(GL.GL_POINTS, 0, f1.capacity()/3);
  gl.glDisableClientState(GL.GL_VERTEX_ARRAY); 
  pgl.endGL();
  
  frame.setTitle("Cloud "+ currentScreen+ ", registered " + cloud1PointsList.size() + " points.");
}

public void drawCloudTwo(){
  
  //Get GL object to make direct OpenGL calls
  PGraphicsOpenGL pgl2 = (PGraphicsOpenGL) g;
  gl2 = pgl2.beginGL();
  gl2.glPointSize(pointSize);
  gl2.glColor4f(0.6f,0.6f,0.5f,0.6f);   
  gl2.glEnableClientState(GL.GL_VERTEX_ARRAY);
  gl2.glVertexPointer(3, GL.GL_FLOAT, 0, f2); //define an array of vertex data, gets points from FloatBuffer f
  gl2.glDrawArrays(GL.GL_POINTS, 0, f2.capacity()/3);
  gl2.glDisableClientState(GL.GL_VERTEX_ARRAY);
  pgl2.endGL();
  
  frame.setTitle("Cloud "+ currentScreen+ ", registered " + cloud2PointsList.size() + " points.");
}



public void drawBigCloud(){
  
  //Get GL object to make direct OpenGL calls
  PGraphicsOpenGL pgl2 = (PGraphicsOpenGL) g;
  gl2 = pgl2.beginGL();
  gl2.glPointSize(pointSize);
  gl2.glColor4f(0.6f,0.6f,0.5f,0.6f);   
  gl2.glEnableClientState(GL.GL_VERTEX_ARRAY);
  gl2.glVertexPointer(3, GL.GL_FLOAT, 0, f2); //define an array of vertex data, gets points from FloatBuffer f
  gl2.glDrawArrays(GL.GL_POINTS, 0, f2.capacity()/3);
  gl2.glDisableClientState(GL.GL_VERTEX_ARRAY);
  
  gl2.glColor4f(0.8f,0.6f,0.6f,0.6f);   
  gl2.glEnableClientState(GL.GL_VERTEX_ARRAY);
  gl2.glVertexPointer(3, GL.GL_FLOAT, 0, f3); //define an array of vertex data, gets points from FloatBuffer f
  gl2.glDrawArrays(GL.GL_POINTS, 0, f3.capacity()/3);
  gl2.glDisableClientState(GL.GL_VERTEX_ARRAY);
  pgl2.endGL();
  frame.setTitle("KinectCloud - Merged cloud");
}

public void shouldCameraBeOn(){
  if(currentScreen == 0){
    camera();
    cameraOn = false;
  }
  else{
    if(cameraOn == false){
      camera1 = new PeasyCam(this,00,00,00, 500);
      camera1.setWheelScale(1);
      camera1.setMinimumDistance(10);
      cameraOn = true;
    }
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "KinectCloudGL" });
  }
}
