/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.control.MailControl;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * L�schen eines Mailanhanges
 */
public class MailAnhangAnzeigeAction implements Action
{

  private MailControl control = null;

  public MailAnhangAnzeigeAction(MailControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof MailAnhang))
    {
      throw new ApplicationException("Keinen Mail-Anhang ausgew�hlt");
    }
    try
    {
      MailAnhang ma = (MailAnhang) context;
      File tmp = new File(System.getProperty("java.io.tmpdir"),
          ma.getDateiname());
      FileOutputStream fos = new FileOutputStream(tmp);
      fos.write(ma.getAnhang());
      fos.close();
      FileViewer.show(tmp);
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim entfernen eines Mailanhanges";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
    catch (FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
