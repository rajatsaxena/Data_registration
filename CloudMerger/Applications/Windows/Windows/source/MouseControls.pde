/*
Methods to handle mouse actions
*/

void mouseClicked(){
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

PVector selectClosestPoint(PVector mouseClick, PVector[] cloud){
  float closeDist = 9999.9999;
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
