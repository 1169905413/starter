//package Adam6052;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
//import java.net.DatagramSocket;
//import java.net.DatagramPacket;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.net.InetAddress;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class Adam6052 extends Applet implements Runnable{
  boolean isStandalone = false;
  String var0;
  Thread AdamPoilThread;
  String HostIP;// = //"0AC.012.001.0C1";//"172.18.1.212";
  boolean AdamRun = false;
  boolean Adamfinish = false;
  byte UserPassWordArray[] = new byte[8];
  CmdComm AdamConn;

  Label Label1 = new Label();
  Led LedDI0 = new Led();
  Led LedDI1 = new Led();
  Led LedDI2 = new Led();
  Led LedDI3 = new Led();
  Led LedDI4 = new Led();
  Led LedDI5 = new Led();
  Led LedDI6 = new Led();
  Led LedDI7 = new Led();
	/*Led LedDI8 = new Led();
	Led LedDI9 = new Led();
	Led LedDI10 = new Led();
	Led LedDI11 = new Led();*/

  Led CommLed = new Led(java.awt.Color.orange.darker().darker(), java.awt.Color.orange.darker().darker().darker().darker().darker());

  //User password login windows layout	2002/8/23
  myFramPanel palMessageWindow = new myFramPanel(3, " Message ", 62);
  Label LabMessage = new Label("Please enter your password:");
  TextField txtUserPassword = new TextField("");
  Button btPW_Apply = new Button("OK");


  Led LedDO0 = new Led();
  Led LedDO1 = new Led();
  Led LedDO2 = new Led();
  Led LedDO3 = new Led();
  Led LedDO4 = new Led();
  Led LedDO5 = new Led();
  Led LedDO6 = new Led();
  Led LedDO7 = new Led();

  Label labDI0 = new Label("DI0");
  Label labDI1 = new Label("DI1");
  Label labDI2 = new Label("DI2");
  Label labDI3 = new Label("DI3");
  Label labDI4 = new Label("DI4");
  Label labDI5 = new Label("DI5");
  Label labDI6 = new Label("DI6");
  Label labDI7 = new Label("DI7");
	/*Label labDI8 = new Label("DI8");
	Label labDI9 = new Label("DI9");
	Label labDI10 = new Label("DI10");
	Label labDI11 = new Label("DI11");*/

  Button btDO0 = new Button("DO0");
  Button btDO1 = new Button("DO1");
  Button btDO2 = new Button("DO2");
  Button btDO3 = new Button("DO3");
  Button btDO4 = new Button("DO4");
  Button btDO5 = new Button("DO5");
  Button btDO6 = new Button("DO6");
  Button btDO7 = new Button("DO7");

  myFramPanel palDIOStatus = new myFramPanel(2);
  myFramPanel palDI = new myFramPanel(3, " Adam DI Status ", 95);
  myFramPanel palDO = new myFramPanel(3, " Adam Status ", 70);
  myFramPanel palAdamStatus = new myFramPanel(1);


  Label labAdamStatusForDIO = new Label("Status : ");

  //Label labDIHighByteValue = new Label("High Byte (Hex)");
  Label labDILowByteValue = new Label("Low Byte (Hex)");
  //TextField txtDIHighByteValue = new TextField("");
  TextField txtDILowByteValue = new TextField("");

  TextField txtDOLowByteValue = new TextField("");
  Label labDOLowByteValue = new Label("Low Byte (Hex)");

  /**Get a parameter value*/
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  /**Construct the applet*/
  public Adam6052() {
  }
  /**Initialize the applet*/
  public void init() {

    try
    {
      HostIP  = "192.168.2.220";
      AdamConn = new CmdComm(HostIP);
      labAdamStatusForDIO.setText("Host IP:" + AdamConn.GetHostIP() + " Please enter password.");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    try {
      SetDIStatus(0) ;
      jbInit();
      AdamPoilThread = new Thread(this);
      AdamPoilThread.start();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    System.out.println("Initialization...");
  }
  public void run() {
    boolean ErrCnt;
    String sRet;
    byte CmdBuf[] = new byte[128];
    long DIOVal;

    ErrCnt = false;

    while (true) {
      int i;

      Adamfinish=false;
      if(AdamRun)
      {
        if(GetDIOStatus(CmdBuf))
        {
          CmdBuf = AdamConn.RecvArrayReturn();
          DIOVal=ASC2HEX((char)CmdBuf[5]);
          DIOVal<<=4;
          DIOVal|=(ASC2HEX((char)CmdBuf[6])&0xf);
          DIOVal<<=4;
          DIOVal|=(ASC2HEX((char)CmdBuf[7])&0xf);
          DIOVal<<=4;
          DIOVal|=(ASC2HEX((char)CmdBuf[8])&0xf);

          //System.out.println("GetDIOStatus: "+DIOVal);	//herela
          UpdateIOLEDStatus(DIOVal);
          labAdamStatusForDIO.setText("Status : Communcation successful. V1.05");// (^_^)~" + "  Counter = " + ErrCnt++);
          CommLed.setLed(ErrCnt);
          if (ErrCnt)	ErrCnt = false;
          else ErrCnt = true;
        }
        else {
          labAdamStatusForDIO.setText("Status : Communcation timeout!! V1.05");// (^\"^)!!" + "  Counter = " + ErrCnt++);
          CommLed.setLed(false);
        }
        Adamfinish=true;
      }
      AdamDelay(500);
    }
  }

  public void start() { }

  public void stop() { }

  public void destroy() {
    AdamPoilThread	.stop();
    AdamPoilThread = null;
  }

  /**Component initialization*/

  private void jbInit() throws Exception {
    int PW_X = 33 , PW_Y = 80;

    this.setLayout(null);
    palMessageWindow.setBackground(Color.lightGray);
    palMessageWindow.setBounds(new Rectangle(PW_X + 12, PW_Y + 15 , 320, 90));
    LabMessage.setFont(new java.awt.Font("DialogInput", 3, 14));

    LabMessage.setBounds(new Rectangle(18, 17, 220, 30));
    txtUserPassword.setBounds(new Rectangle(240, 22, 60, 20));
    btPW_Apply.setBounds(new Rectangle(130, 22 + 30, 59, 25));
    txtUserPassword.setForeground(Color.darkGray);
    txtUserPassword.setBackground(Color.darkGray);


    palDIOStatus.add(palMessageWindow, null);
    palMessageWindow.add(LabMessage, null);
    palMessageWindow.add(txtUserPassword, null);
    palMessageWindow.add(btPW_Apply, null);

    btDO0.setForeground(Color.darkGray);
    btDO1.setForeground(Color.darkGray);
    btDO2.setForeground(Color.darkGray);
    btDO3.setForeground(Color.darkGray);
    btDO4.setForeground(Color.darkGray);
    btDO5.setForeground(Color.darkGray);

    palDIOStatus.setBackground(Color.lightGray);
    palAdamStatus.setBackground(Color.lightGray);

    //palDIOStatus.setBounds(new Rectangle(42, 50, 409, 15 *2 + 0 * 2 + 110 + 152 + 33 ));
    palDIOStatus.setBounds(new Rectangle(42, 66, 409, 369 + 33));
    //palDI.setBounds(new Rectangle(12, 15 , 385, 110));
    palDI.setBounds(new Rectangle(12, 7 + 7, 385, 100 + 10));
    //palDO.setBounds(new Rectangle(12, 15 + 110 + 0 , 385, 152));
    palDO.setBounds(new Rectangle(12, 103 + 7 + 10 , 385, 152));
    //palAdamStatus.setBounds(new Rectangle(12, 15 + 110 + 0 * 2 + 152, 385, 33));
    palAdamStatus.setBounds(new Rectangle(14, 316 -58 + 7 + 10, 381, 33));

    palDIOStatus.setLayout(null);
    palDI.setLayout(null);
    palDO.setLayout(null);
    palAdamStatus.setLayout(null);

    DIOLayout();

    Label1.setFont(new java.awt.Font("DialogInput", 3, 26));
    Label1.setForeground(Color.blue);
    Label1.setText("ADAM-6052 DIO Module");
    Label1.setBounds(new Rectangle(83, 17, 326, 29));
    this.add(Label1, null);
    this.add(palDIOStatus, null);

    //2002/8/23
    //palDI.disable();
    palDIOStatus.add(palDI, null);
    palDI.add(LedDI0, null);
    palDI.add(LedDI1, null);
    palDI.add(LedDI2, null);
    palDI.add(LedDI3, null);
    palDI.add(LedDI4, null);
    palDI.add(LedDI5, null);
    palDI.add(LedDI6, null);
    palDI.add(LedDI7, null);
		/*palDI.add(LedDI8, null);
		palDI.add(LedDI9, null);
		palDI.add(LedDI10, null);
		palDI.add(LedDI11, null);*/

    palDI.add(labDI0, null);
    palDI.add(labDI1, null);
    palDI.add(labDI2, null);
    palDI.add(labDI3, null);
    palDI.add(labDI4, null);
    palDI.add(labDI5, null);
    palDI.add(labDI6, null);
    palDI.add(labDI7, null);
		/*palDI.add(labDI8, null);
		palDI.add(labDI9, null);
		palDI.add(labDI10, null);
		palDI.add(labDI11, null);*/

    //palDI.add(labDIHighByteValue, null);
    //palDI.add(txtDIHighByteValue, null);
    palDI.add(labDILowByteValue, null);
    palDI.add(txtDILowByteValue, null);

    palDO.disable();
    palDIOStatus.add(palDO, null);
    palDO.add(LedDO0, null);
    palDO.add(LedDO1, null);
    palDO.add(LedDO2, null);
    palDO.add(LedDO3, null);
    palDO.add(LedDO4, null);
    palDO.add(LedDO5, null);
    palDO.add(LedDO6, null);
    palDO.add(LedDO7, null);
    palDO.add(btDO0, null);
    palDO.add(btDO1, null);
    palDO.add(btDO2, null);
    palDO.add(btDO3, null);
    palDO.add(btDO4, null);
    palDO.add(btDO5, null);
    palDO.add(btDO6, null);
    palDO.add(btDO7, null);

    palDO.add(txtDOLowByteValue, null);
    palDO.add(labDOLowByteValue, null);

    palDIOStatus.add(palAdamStatus, null);

    labAdamStatusForDIO.setBounds(new Rectangle(10, 8, 300, 12));
    palAdamStatus.add(labAdamStatusForDIO, null);
    palAdamStatus.add(CommLed, null);
    CommLed.setBounds(new Rectangle(355, 7, 18, 18));
  }



  void DIOLayout() {
    int x,  y, X_Spe, Y_Spe, Height, Weight;
    int X_Off, Y_Off;

    X_Off = 15; Y_Off = 5;
    x = 20 + X_Off; y = 20 + Y_Off; Height = 20; Weight = 20;
    X_Spe = 60; Y_Spe = 35;

    LedDI0.setBounds(new Rectangle(x, y, Weight, Height));
    LedDI1.setBounds(new Rectangle(x + X_Spe, y, Weight, Height));
    LedDI2.setBounds(new Rectangle(x + X_Spe * 2, y, Weight, Height));
    LedDI3.setBounds(new Rectangle(x + X_Spe * 3, y, Weight, Height));

    LedDI4.setBounds(new Rectangle(x, y + Y_Spe, Weight, Height));
    LedDI5.setBounds(new Rectangle(x + X_Spe, y + Y_Spe, Weight, Height));
    LedDI6.setBounds(new Rectangle(x + X_Spe * 2, y + Y_Spe, Weight, Height));
    LedDI7.setBounds(new Rectangle(x + X_Spe * 3, y + Y_Spe, Weight, Height));

    /*LedDI8.setBounds(new Rectangle(x, y + Y_Spe * 2, Weight, Height));
    LedDI9.setBounds(new Rectangle(x + X_Spe, y + Y_Spe *2, Weight, Height));
    LedDI10.setBounds(new Rectangle(x + X_Spe * 2, y + Y_Spe * 2, Weight, Height));
    LedDI11.setBounds(new Rectangle(x + X_Spe * 3, y + Y_Spe * 2, Weight, Height));*/


    x = 20 + X_Off; y = 32 + Y_Off; Height = 30; Weight = 40;

    labDI0.setBounds(new Rectangle(x, y, Weight, Height));
    labDI1.setBounds(new Rectangle(x + X_Spe, y, Weight, Height));
    labDI2.setBounds(new Rectangle(x + X_Spe * 2, y, Weight, Height));
    labDI3.setBounds(new Rectangle(x + X_Spe * 3, y, Weight, Height));

    labDI4.setBounds(new Rectangle(x, y + Y_Spe, Weight, Height));
    labDI5.setBounds(new Rectangle(x + X_Spe, y + Y_Spe, Weight, Height));
    labDI6.setBounds(new Rectangle(x + X_Spe * 2, y + Y_Spe, Weight, Height));
    labDI7.setBounds(new Rectangle(x + X_Spe * 3, y + Y_Spe, Weight, Height));

    /*labDI8.setBounds(new Rectangle(x, y + Y_Spe * 2, Weight, Height));
    labDI9.setBounds(new Rectangle(x + X_Spe, y + Y_Spe *2, Weight, Height));
    labDI10.setBounds(new Rectangle(x + X_Spe * 2, y + Y_Spe * 2, Weight, Height));
    labDI11.setBounds(new Rectangle(x + X_Spe * 3, y + Y_Spe * 2, Weight, Height));*/

    //X_Off = 5; Y_Off = 5;
    x = 20 + X_Off; y = 20 + Y_Off; Height = 20; Weight = 20;
    X_Spe = 60; Y_Spe = 35;

    LedDO0.setBounds(new Rectangle(x, y, Weight, Height));
    LedDO1.setBounds(new Rectangle(x + X_Spe, y, Weight, Height));
    LedDO2.setBounds(new Rectangle(x + X_Spe * 2, y, Weight, Height));
    LedDO3.setBounds(new Rectangle(x + X_Spe * 3, y, Weight, Height));

    x = 20 + X_Off; y = 80 + Y_Off; Height = 20; Weight = 20;
    //X_Spe = 60; Y_Spe = 35;

    LedDO4.setBounds(new Rectangle(x, y, Weight, Height));
    LedDO5.setBounds(new Rectangle(x + X_Spe, y, Weight, Height));
    LedDO6.setBounds(new Rectangle(x + X_Spe * 2, y, Weight, Height));
    LedDO7.setBounds(new Rectangle(x + X_Spe * 3, y, Weight, Height));

    X_Off = 5; Y_Off = 5;
    x = 10 + X_Off; y = 45 + Y_Off; Height = 25; Weight = 59;
    X_Spe = 60; Y_Spe = 35;

    btDO0.setBounds(new Rectangle(x, y, Weight, Height));
    btDO1.setBounds(new Rectangle(x + X_Spe, y, Weight, Height));
    btDO2.setBounds(new Rectangle(x + X_Spe * 2, y, Weight, Height));
    btDO3.setBounds(new Rectangle(x + X_Spe * 3, y, Weight, Height));

    //X_Off = 5; Y_Off = 5;
    x = 10 + X_Off; y = 105 + Y_Off; Height = 25; Weight = 59;
    //X_Spe = 60; Y_Spe = 35;

    btDO4.setBounds(new Rectangle(x, y, Weight, Height));
    btDO5.setBounds(new Rectangle(x + X_Spe, y, Weight, Height));
    btDO6.setBounds(new Rectangle(x + X_Spe * 2, y, Weight, Height));
    btDO7.setBounds(new Rectangle(x + X_Spe * 3, y, Weight, Height));

    //labDIHighByteValue.setBounds(new Rectangle(273, 74, 100, 21));
    labDILowByteValue.setBounds(new Rectangle(273, 22, 100, 21));

    //txtDIHighByteValue.setFont(new java.awt.Font("SansSerif", 1, 14));
    //txtDIHighByteValue.setEditable(false);
    txtDILowByteValue.setFont(new java.awt.Font("SansSerif", 1, 14));
    txtDILowByteValue.setEditable(false);

    txtDILowByteValue.setBounds(new Rectangle(273, 47, 47, 21));
    //txtDIHighByteValue.setBounds(new Rectangle(273, 102, 47, 21));

    txtDOLowByteValue.setBounds(new Rectangle(273, 47, 47, 21));
    txtDOLowByteValue.setEditable(false);
    txtDOLowByteValue.setFont(new java.awt.Font("SansSerif", 1, 14));
    labDOLowByteValue.setBounds(new Rectangle(273, 22, 100, 21));

    btDO0.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(0);
      }
    });
    btDO1.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(1);
      }
    });
    btDO2.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(2);
      }
    });
    btDO3.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(3);
      }
    });
    btDO4.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(4);
      }
    });
    btDO5.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(5);
      }
    });
    btDO6.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(6);
      }
    });
    btDO7.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        SetDOutput(7);
      }
    });

    btPW_Apply.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        UserPassWordArray = txtUserPassword.getText().getBytes();
        if (ChkPW(UserPassWordArray, txtUserPassword.getText().length()) && txtUserPassword.getText().length() > 0)
        {
          //txtUserPassword.setText("PASS");
          //palDI.enable();
          palDO.SetTitleMsg(" Adam DO Status(Operating Mode) ", 190);
          palDO.enable();
          btDO0.setForeground(Color.black);
          btDO1.setForeground(Color.black);
          btDO2.setForeground(Color.black);
          btDO3.setForeground(Color.black);
          btDO4.setForeground(Color.black);
          btDO5.setForeground(Color.black);
          btDO6.setForeground(Color.black);
          btDO7.setForeground(Color.black);
          palMessageWindow.setVisible(false);

        }
        else
        {
          palDO.SetTitleMsg(" Adam DO Status(Monitoring Mode) ", 200);
          palMessageWindow.setVisible(false);
          //txtUserPassword.setText("FAIL");
        }
        AdamRun=true;
      }
    });
  }
  /**Get Applet information*/
  public String getAppletInfo() {
    return "Applet Information";
  }
  /**Get parameter info*/
  public String[][] getParameterInfo() {
    String[][] pinfo =
      {
        {"HostIP", "String", ""},
      };
    return pinfo;
  }


  public String ConvertStrToHex(int Data) {
    String tmp;
    tmp = Long.toHexString((int)Data);
    if (tmp.length() > 2)
      tmp = tmp.substring(tmp.length() - 2);
    if (tmp.length() == 1)
      tmp = "0" + tmp;
    return tmp.toUpperCase();
  }

  void SetDOutput(int Ch)  {
    long lData;
    byte CmdBuf[] = new byte[128];

    while(true)
    {
      if(Adamfinish)
      {
        AdamRun=false;
        break;
      }
    }


    lData=GetDIStatus();
    if((lData&(0x1<<(Ch+8)))>0)
      lData&=~(0x1<<(Ch+8));
    else
      lData|=(0x1<<(Ch+8));

    CmdBuf[0] = '#';
    CmdBuf[1] = '0';
    CmdBuf[2] = '1';
    CmdBuf[3] = '0';
    CmdBuf[4] = '0';
    CmdBuf[5] = (byte)HEX2ASC((byte)((lData>>12)&0xf));
    CmdBuf[6] = (byte)HEX2ASC((byte)((lData>>8)&0xf));
    CmdBuf[7] = 0x0d;
    CmdBuf[8] = 0;

    //System.out.println("SetDO: "+lData);

    if(AdamConn.SendCmd2Adam(CmdBuf, 8))
    {

      try
      {
        CmdBuf = AdamConn.RecvArrayReturn();
        if (((CmdBuf[0] == '!')||(CmdBuf[0] == '>')) && (CmdBuf[1] == '0') && (CmdBuf[2] == '1'))
        {
          UpdateIOLEDStatus(lData);
          //return true;
        }
        else
        {
          CmdBuf[20]=0;
          System.out.println("fail with command: "+CmdBuf);
          //return false;
        }
      }
      catch(Exception e)
      {
        System.out.println("Compare value exception!");
      }
    }
    else
    {
      System.out.println("Send command fail!");
      //return false;
    }
    AdamRun=true;

  }


  long DIO_Status;

  long GetDIStatus()  {
    return DIO_Status;
  }

  boolean Is1stDIStatusUpdate = true;

  void SetDIStatus(long lData)  {
    DIO_Status = lData;
  }

  void UpdateIOLEDStatus(long DIOVal)
  {
    int DI_Hi_Byte, DI_Low_Byte;
    int DO_Low_Byte;
    long Data;

    DO_Low_Byte=((int)DIOVal>>8)&0xff;

    DI_Low_Byte=((int)DIOVal)&0xff;

    Data=DIOVal;

    if ((Data == GetDIStatus()) && (Is1stDIStatusUpdate == false))
      return;
    else
      Is1stDIStatusUpdate = false;

    txtDILowByteValue.setText("0x" + ConvertStrToHex((int)DI_Low_Byte));
    txtDOLowByteValue.setText("0x" + ConvertStrToHex((int)DO_Low_Byte));

    if((DI_Low_Byte & 0x01) >= 1)
      LedDI0.setLed(true);
    else  LedDI0.setLed(false);

    if((DI_Low_Byte & 0x02) >= 1)
      LedDI1.setLed(true);
    else  LedDI1.setLed(false);

    if((DI_Low_Byte & 0x04) >= 1)
      LedDI2.setLed(true);
    else  LedDI2.setLed(false);

    if((DI_Low_Byte & 0x08) >= 1)
      LedDI3.setLed(true);
    else  LedDI3.setLed(false);

    if((DI_Low_Byte & 0x10) >= 1)
      LedDI4.setLed(true);
    else  LedDI4.setLed(false);

    if((DI_Low_Byte & 0x20) >= 1)
      LedDI5.setLed(true);
    else  LedDI5.setLed(false);

    if((DI_Low_Byte & 0x40) >= 1)
      LedDI6.setLed(true);
    else  LedDI6.setLed(false);

    if((DI_Low_Byte & 0x80) >= 1)
      LedDI7.setLed(true);
    else  LedDI7.setLed(false);



    if((DO_Low_Byte & 0x1) >= 1)
      LedDO0.setLed(true);
    else  LedDO0.setLed(false);

    if((DO_Low_Byte & 0x2) >= 1)
      LedDO1.setLed(true);
    else  LedDO1.setLed(false);

    if((DO_Low_Byte & 0x4) >= 1)
      LedDO2.setLed(true);
    else  LedDO2.setLed(false);

    if((DO_Low_Byte & 0x8) >= 1)
      LedDO3.setLed(true);
    else  LedDO3.setLed(false);

    if((DO_Low_Byte & 0x10) >= 1)
      LedDO4.setLed(true);
    else  LedDO4.setLed(false);

    if((DO_Low_Byte & 0x20) >= 1)
      LedDO5.setLed(true);
    else  LedDO5.setLed(false);

    if((DO_Low_Byte & 0x40) >= 1)
      LedDO6.setLed(true);
    else  LedDO6.setLed(false);

    if((DO_Low_Byte & 0x80) >= 1)
      LedDO7.setLed(true);
    else  LedDO7.setLed(false);

    SetDIStatus(Data) ;

  }

  char HEX2ASC( byte hex)
  {
    int asc;

    if(( hex >=0)&&( hex <=9))
      asc = hex+0x30;
    else  if(( hex >=10)&&( hex <=15))
      asc = hex-10+0x41;
    else
      asc = 0xff;
    return (char)(asc&0xff);
  }

  byte ASC2HEX(int c)
  {
    if (( c >='0')&&(c <='9'))
    {
      c=(c -0x30);
    }
    else if (( c >='A')&&(c <='F'))
    {
      c=(c -0x41+10);
    }
    else if (( c >='a')&&(c <='f'))
    {
      c=(c -0x61+10);
    }

    return (byte)(c&0xf);
  }

  void AdamDelay(int DT) {
    try  {
      Thread.sleep(DT);
    }  catch(Exception eSleep)  {}
  }


  void EncodePassWord(byte PassWord[])
  {
    int i;

    for ( i = 0; i < 8; i++ )
    {
      PassWord[i + 6] ^= 0x3f;
    }

    return;
  };

  public boolean GetDIOStatus(byte Resp[])
  {
    int i;
    byte Command[] = new byte[128];

    Command[0] = '$';
    Command[1] = '0';
    Command[2] = '1';
    Command[3] = '6';
    Command[4] = 0x0d;

    if(AdamConn.SendCmd2Adam(Command, 5))
    {

      try
      {
        Command = AdamConn.RecvArrayReturn();
        if (((Command[0] == '!')||(Command[0] == '>')) && (Command[1] == '0') && (Command[2] == '1'))
        {
          return true;
        }
        else
        {
          Command[20]=0;
          System.out.println(Command);
          return false;
        }
      }
      catch(Exception e)
      {
        System.out.println("Compare value exception!");
      }
    }
    else
    {
      System.out.println("Send command fail!");
      return false;
    }

    return true;
  };

  public boolean ChkPW(byte PassWord[], int Len)
  {
    int i;
    byte CmdBuf[] = new byte[128];
    CmdBuf[0] = '$';
    CmdBuf[1] = '0';
    CmdBuf[2] = '1';
    CmdBuf[3] = 'P';
    CmdBuf[4] = 'W';
    CmdBuf[5] = '0';

    for (i = 0; i < 8; i++)
    {
      try
      {
        CmdBuf[6 + i] = PassWord[i];
      }
      catch(Exception e) {
        CmdBuf[6 + i] = 0x31;
      }
    }
    CmdBuf[6 + i] = 0x0d;

    EncodePassWord(CmdBuf);



//    if(AdamConn.SendCmd2Adam(CmdBuf, 15))
//    {
//      try
//      {
//        if (((AdamConn.RecvByteReturn(0) == '!')||(AdamConn.RecvByteReturn(0) == '>')) && AdamConn.RecvByteReturn(1) == '0' && AdamConn.RecvByteReturn(2) == '1')
//          return true;
//        else return false || true;
//      }
//      catch(Exception e) {e.printStackTrace();}
//    }

    return true;
  };

  /**Main method*/
  public static void main(String[] args) {
    Adam6052 applet = new Adam6052();
    applet.isStandalone = true;
    Frame frame;
    frame = new Frame() {
      protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title) {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle("Applet Frame");
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(500,620);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }

}


class CmdComm extends Object	//herela
{
  int port;
  InetAddress address;
  DatagramSocket socket = null;
  DatagramPacket SendPacket;
  DatagramPacket RecvPacket;
  String myHost;
  byte[] RecvBuf = new byte[128];



  public CmdComm(String Host)
  {

    try
    {
      //System.out.println("New CmdComm!");
      socket = new DatagramSocket();
      socket.setSoTimeout(3000);	//timeout 2 seconds
    }
    catch(Exception e)
    {
      System.out.println("New CmdComm fail!");
    }
    myHost = chkHostIP(Host);
    try
    {
      //System.out.println("Get Host!");
      address = InetAddress.getByName(myHost);
    }
    catch(Exception e)
    {
      System.out.println("Get Host fail!");
    }
  }

  public boolean SendCmd2Adam(byte CmdBuf[], int CmdLen)	//herela
  {
    int RetLen, i;
//    if(CmdLen == 8) {

      for (int ix = 0 ; ix < CmdLen ; ix ++) {
        char c = (char) CmdBuf[ix];
        System.out.print(c);
      }
       System.out.println();
      for (int ix = 0 ; ix < CmdLen ; ix ++) {
        char c = (char) CmdBuf[ix];
        System.out.print(CmdBuf[ix] + " ");
      }
      System.out.println("\n-------");
//    }
//    System.out.println(CmdBuf.length + " = " + CmdLen);
//    for(byte b : CmdBuf) {
//      System.out.print(b + " ");
//    }
    try
    {
      SendPacket = new DatagramPacket(CmdBuf, CmdLen,
        address, 1025);
      socket.send(SendPacket);
      RecvPacket = new DatagramPacket(RecvBuf, RecvBuf.length);
      socket.receive(RecvPacket);
      if(RecvPacket.getLength()<=0)
      {
        System.out.println("Recv <=0");
        return false;
      }
      else
      {
      }

    }
    catch (IOException e)
    {
      System.out.println("SendCmd2Adam fail!");
      return false;
    }
    return true;
  }

  public byte RecvByteReturn(int idx)
  {
    return RecvBuf[idx];
  }

  public byte[] RecvArrayReturn()
  {
    return RecvBuf;
  }

  public String GetHostIP() {
    return myHost;
  }

  private	String chkHostIP(String Host)
  {
    String tmp = "";

    if (Host.length() < 15)	return Host;

    for (int i = 0; i < 16; i += 4)
    {
      tmp += (String.valueOf(Long.valueOf(Host.substring(i, i + 3), 16)) + '.');
    }

    return tmp.substring(0, tmp.length() - 1);
  }

}



class myFramPanel extends Panel
{
  int panelType;
  Label labMassage = new Label("");

  public myFramPanel() {
    super();
  }

  public myFramPanel(int myType) {
    super();
    panelType = myType;
  }

  public myFramPanel(int myType, String Msg, int msgTextLength) {
    super();
    panelType = myType;
    if (Msg != "") {
      labMassage.setText(Msg);
      this.setLayout(null);
      labMassage.setBounds(new Rectangle(20, 3, msgTextLength, 15));
      this.add(labMassage);
    }
  }

  public void SetTitleMsg(String Msg, int msgTextLength)
  {
    labMassage.setText(Msg);
    labMassage.setBounds(new Rectangle(20, 3, msgTextLength, 15));
  }

  public void paint(Graphics g) {
    Dimension size = getSize();

    if (panelType == 1) {
      int off;

      off = 4;
      g.setColor(Color.white);
      g.drawRect(0, 0, size.width - 1, size.height - 1);

      g.setColor(Color.darkGray);
      g.drawLine(size.width - 1, 0, size.width - 1, size.height - 1);
      g.drawLine(0, size.height - 1, size.width - 1, size.height - 1);g.setColor(Color.black);

      g.setColor(Color.black);
      g.drawRect(off, off, size.width - 2 - off * 2, size.height - 2 - off * 2);
    }
    else if (panelType == 2)	{
      //int off;

      //off = 4;
      g.setColor(Color.white);
      g.drawRect(0, 0, size.width - 1, size.height - 1);

      g.drawLine(size.width - 4, 2, size.width - 4, size.height - 4);
      g.drawLine(2, size.height - 4, size.width - 4, size.height - 4);

      g.setColor(Color.darkGray);
      g.drawLine(2, 2, size.width - 4, 2);
      g.drawLine(2, 2, 2, size.height - 4);

      g.drawLine(size.width - 1, 0, size.width - 1, size.height - 1);
      g.drawLine(0, size.height - 1, size.width - 1, size.height - 1);g.setColor(Color.black);
    }
    else if (panelType == 3) {
      int off;

      off = 4;
      g.setColor(Color.white);
      g.drawRect(0, 0, size.width - 1, size.height - 1);

      g.setColor(Color.darkGray);
      g.drawLine(size.width - 1, 0, size.width - 1, size.height - 1);
      g.drawLine(0, size.height - 1, size.width - 1, size.height - 1);

      g.setColor(Color.black);
      g.drawRect(off, off + 5, size.width - 2 - off * 2, size.height - 2 - off * 2 -5 );
    }
    else {
      g.setColor(Color.darkGray);
      g.drawRect(0, 0, size.width - 1, size.height - 1);
    }
  }
};

/**
 * Insert the type's description here.
 * Creation date: (00-04-28 坷饶 1:42:39)
 * @author:
 */
class Led extends Canvas {//VisibleCanvas {
  private java.awt.Color fieldTrueColor = new java.awt.Color(0);
  private java.awt.Color fieldFalseColor = new java.awt.Color(0);
  private boolean fieldStatus = false;
  private int LedType = 0;
  /**
   * Led constructor comment.
   */
  public Led() {
    super();
    initialize();
    LedType = 0;
  }

  public Led(Color LedTrue, Color LedFalse) {
    super();
    initialize();
    setFalseColor(LedFalse);
    setTrueColor(LedTrue);
    LedType = 1;
  }
  /**
   * Gets the falseColor property (java.awt.Color) value.
   * @return The falseColor property value.
   * @see #setFalseColor
   */
  public java.awt.Color getFalseColor() {
    return fieldFalseColor;
  }
  /**
   * Gets the status property (boolean) value.
   * @return The status property value.
   * @see #setStatus
   */
  public boolean getStatus() {
    return fieldStatus;
  }
  /**
   * Gets the trueColor property (java.awt.Color) value.
   * @return The trueColor property value.
   * @see #setTrueColor
   */
  public java.awt.Color getTrueColor() {
    return fieldTrueColor;
  }
  /**
   * Called whenever the part throws an exception.
   * @param exception java.lang.Throwable
   */
  private void handleException(java.lang.Throwable exception) {

    /* Uncomment the following lines to print uncaught exceptions to stdout */
    // System.out.println("--------- UNCAUGHT EXCEPTION ---------");
    // exception.printStackTrace(System.out);
  }
  /**
   * Initialize the class.
   */
  /* WARNING: THIS METHOD WILL BE REGENERATED. */
  private void initialize() {
    try {
      // user code begin {1}
      // user code end
      setName("Led");
      //setSize(20, 20);
      //setBounds(new Rectangle(50, 50, 20, 20));
      setFalseColor(java.awt.Color.red);
      //setTrueColor(java.awt.Color.gray);
      setTrueColor(java.awt.Color.green);
    } catch (java.lang.Throwable ivjExc) {
      handleException(ivjExc);
    }
    // user code begin {2}
    // user code end
  }
  /**
   * main entrypoint - starts the part when it is run as an application
   * @param args java.lang.String[]
   */

  public static void main(java.lang.String[] args) {
    try {
      Frame frame = new java.awt.Frame();
      Led aLed;
      aLed = new Led();
      frame.add("Center", aLed);
      frame.setSize(aLed.getSize());
      frame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        };
      });
      frame.setVisible(true);
    } catch (Throwable exception) {
      System.err.println("Exception occurred in main() of demobeans.Led");
      exception.printStackTrace(System.out);
    }
  }

  public void paint(Graphics g) {
    Color dark, bright;
    Dimension size = getSize();
    if (fieldStatus == false) {
      dark = fieldFalseColor.darker().darker();
      bright = fieldFalseColor.brighter().brighter();
      dark = bright;
    } else {
      dark = fieldTrueColor.darker().darker();
      bright = fieldTrueColor.brighter().brighter();
      dark = bright;
    }
    int sgr = size.width < size.height ? size.width : size.height;
    int sx = 0;
    int sy = 0;
    g.setColor(Color.white);
    g.fillArc(sx, sy, sgr, sgr, 45, -180);
    g.setColor(new Color(128, 128, 128));
    g.fillArc(sx, sy, sgr, sgr, 45, 180);
    g.setColor(Color.black);
    g.fillOval(sx + 2, sy + 2, sgr - 4, sgr - 4);
    g.setColor(dark);
    g.fillOval(sx + 3, sy + 3, sgr - 6, sgr - 6);

    if (LedType == 1) {
      if (fieldStatus == true)
      {
        g.setColor(Color.white);
        g.drawArc(sx + 5, sy + 5, sgr - 12, sgr - 12, 85, 110);
        g.drawArc(sx + 6, sy + 6, sgr - 14, sgr - 14, 85, 130);
      }
    }
    else	{
      g.setColor(Color.white);
      g.drawArc(sx + 5, sy + 5, sgr - 12, sgr - 12, 85, 110);
      g.drawArc(sx + 6, sy + 6, sgr - 14, sgr - 14, 85, 130);
    }


    g.setColor(bright);
    g.drawArc(sx + 4, sy + 4, sgr - 8, sgr - 8, 30, -160);
    g.drawArc(sx + 5, sy + 5, sgr - 10, sgr - 10, 30, -150);
    g.drawArc(sx + 6, sy + 6, sgr - 12, sgr - 12, 10, -80);
  }
  /**
   * Sets the falseColor property (java.awt.Color) value.
   * @param falseColor The new value for the property.
   * @see #getFalseColor
   */
  void setFalseColor(java.awt.Color falseColor) {
    fieldFalseColor = falseColor;
  }
  public void setLed(boolean status) {
    setStatus(status);
    repaint();
  }
  /**
   * Sets the status property (boolean) value.
   * @param status The new value for the property.
   * @see #getStatus
   */
  public void setStatus(boolean status) {
    fieldStatus = status;
  }
  /**
   * Sets the trueColor property (java.awt.Color) value.
   * @param trueColor The new value for the property.
   * @see #getTrueColor
   */
  public void setTrueColor(java.awt.Color trueColor) {
    fieldTrueColor = trueColor;
  }



}

