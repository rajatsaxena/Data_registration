import processing.opengl.*;
import SimpleOpenNI.*;


SimpleOpenNI context;
float        zoomF =0.5f;
float        rotX = radians(180);  
float        rotY = radians(0);

boolean recording = false;
ArrayList<PVector> pts = new ArrayList<PVector>();//points for one frame

void setup()
{
  size(1024,768,OPENGL);  

  context = new SimpleOpenNI(this);
  context.setMirror(false);
  context.enableDepth();
  //context.enableScene();

  stroke(255);
  smooth();  
  perspective(95,float(width)/float(height), 10,150000);
 }

void draw()
{
  context.update();
  background(0);

  translate(width/2, height/2, 0);
  rotateX(rotX);
  rotateY(rotY);
  scale(zoomF);

  int[]   depthMap = context.depthMap();
  //int[]   sceneMap = context.sceneMap();
  int     steps   = 3;  
  int     index;
  PVector realWorldPoint;
  pts.clear();//reset points
  translate(0,0,-1000);  
  //*
  //stroke(100); 
  for(int y=0;y < context.depthHeight();y+=steps)
  {
    for(int x=0;x < context.depthWidth();x+=steps)
    {
      index = x + y * context.depthWidth();
      if(depthMap[index] > 0)
      { 
        realWorldPoint = context.depthMapRealWorld()[index];
        stroke(0,255,0);
        point(realWorldPoint.x,realWorldPoint.y,realWorldPoint.z);
        pts.add(realWorldPoint.get());//store each point
      }
    } 
  } 
  if(recording){
      savePLY(pts);//save to disk as PLY
      //saveCSV(pts);//save to disk as CSV
  }
  //*/
}

// -----------------------------------------------------------------
// Keyboard events

void keyPressed()
{

  switch(key)
  {
    case ' ':
      context.setMirror(!context.mirror());
    break;
    case 'r':
      recording = !recording;
    break;
  }

  switch(keyCode)
  {
    case LEFT:
      rotY += 0.1f;
      break;
    case RIGHT:
      // zoom out
      rotY -= 0.1f;
      break;
    case UP:
      if(keyEvent.isShiftDown())
        zoomF += 0.01f;
      else
        rotX += 0.1f;
      break;
    case DOWN:
      if(keyEvent.isShiftDown())
      {
        zoomF -= 0.01f;
        if(zoomF < 0.01)
          zoomF = 0.01;
      }
      else
        rotX -= 0.1f;
      break;
  }
}
void savePLY(ArrayList<PVector> pts){
  String ply = "ply\n";
  ply += "format ascii 1.0\n";
  ply += "element vertex " + pts.size() + "\n";
  ply += "property float x\n";
  ply += "property float y\n";
  ply += "property float z\n";
  ply += "end_header\n";
  for(PVector p : pts)ply += p.x + " " + p.y + " " + p.z + "\n";
  saveStrings("frame_"+frameCount+".ply",ply.split("\n"));
}

void saveCSV(ArrayList<PVector> pts){
  String csv = "x,y,z\n";
  for(PVector p : pts) csv += p.x + "," + p.y + "," + p.z + "\n";
  saveStrings("frame_"+frameCount+".csv",csv.split("\n"));
}
