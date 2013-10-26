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
package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeAction;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.ZusatzbetraegeMenu;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class ZusatzbetragControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings;

  private DateInput faelligkeit = null;

  private TextInput buchungstext;

  private TextInput buchungstext2;

  private DecimalInput betrag;

  private Zusatzbetrag zuab;

  private DateInput startdatum;

  private SelectInput intervall;

  private DateInput endedatum;

  private DateInput ausfuehrung = null;

  private SelectInput ausfuehrungSuch = null;

  private TablePart zusatzbetraegeList;

  public ZusatzbetragControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Zusatzbetrag getZusatzbetrag()
  {
    if (zuab != null)
    {
      return zuab;
    }
    zuab = (Zusatzbetrag) getCurrentObject();
    return zuab;
  }

  public DateInput getFaelligkeit() throws RemoteException
  {
    if (faelligkeit != null)
    {
      return faelligkeit;
    }

    Date d = getZusatzbetrag().getFaelligkeit();

    this.faelligkeit = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.faelligkeit.setTitle("Fälligkeit");
    this.faelligkeit.setText("Bitte Fälligkeitsdatum wählen");
    this.faelligkeit.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) faelligkeit.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return faelligkeit;
  }

  public TextInput getBuchungstext() throws RemoteException
  {
    if (buchungstext != null)
    {
      return buchungstext;
    }
    buchungstext = new TextInput(getZusatzbetrag().getBuchungstext(), 27);
    buchungstext.setMandatory(true);
    buchungstext.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    return buchungstext;
  }

  public TextInput getBuchungstext2() throws RemoteException
  {
    if (buchungstext2 != null)
    {
      return buchungstext2;
    }
    buchungstext2 = new TextInput(getZusatzbetrag().getBuchungstext2(), 27);
    buchungstext2.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    return buchungstext2;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getZusatzbetrag().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setMandatory(true);
    return betrag;
  }

  public DateInput getStartdatum(boolean withFocus) throws RemoteException
  {
    if (startdatum != null)
    {
      return startdatum;
    }

    Date d = getZusatzbetrag().getStartdatum();
    this.startdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.startdatum.setTitle("Startdatum");
    this.startdatum.setText("Bitte Startdatum wählen");
    this.startdatum.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) startdatum.getValue();
        if (date == null)
        {
          return;
        }
        startdatum.setValue(date);
        if (faelligkeit.getValue() == null)
        {
          faelligkeit.setValue(startdatum.getValue());
        }
      }
    });
    if (withFocus)
    {
      startdatum.focus();
    }
    return startdatum;
  }

  public SelectInput getIntervall() throws RemoteException
  {
    if (intervall != null)
    {
      return intervall;
    }
    Integer i = getZusatzbetrag().getIntervall();
    if (i == null)
    {
      i = Integer.valueOf(0);
    }
    this.intervall = new SelectInput(IntervallZusatzzahlung.getArray(),
        new IntervallZusatzzahlung(i));
    return intervall;
  }

  public DateInput getEndedatum() throws RemoteException
  {
    if (endedatum != null)
    {
      return endedatum;
    }

    Date d = getZusatzbetrag().getEndedatum();
    this.endedatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.endedatum.setTitle("Endedatum");
    this.endedatum.setText("Bitte Endedatum wählen");
    this.endedatum.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) endedatum.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return endedatum;
  }

  public DateInput getAusfuehrung() throws RemoteException
  {
    if (ausfuehrung != null)
    {
      return ausfuehrung;
    }

    Date d = getZusatzbetrag().getAusfuehrung();

    this.ausfuehrung = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.ausfuehrung.setTitle("Ausführung");
    this.ausfuehrung.setText("Bitte Ausführungsdatum wählen");
    this.ausfuehrung.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) ausfuehrung.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    ausfuehrung.setEnabled(false);
    return ausfuehrung;
  }

  public SelectInput getAusfuehrungSuch() throws RemoteException
  {
    if (ausfuehrungSuch != null)
    {
      return ausfuehrungSuch;
    }

    final Vector<String> werte = new Vector<String>();
    werte.addElement("Alle");
    werte.addElement("Aktive");
    werte.addElement("Noch nicht ausgeführt");

    String sql = "select ausfuehrung from zusatzabbuchung where ausfuehrung is not null "
        + "group by ausfuehrung order by ausfuehrung desc";
    DBService service = Einstellungen.getDBService();

    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        while (rs.next())
        {
          werte.addElement(new JVDateFormatTTMMJJJJ().format(rs.getDate(1)));
        }
        return null;
      }
    };
    service.execute(sql, new Object[] {}, rs);

    ausfuehrungSuch = new SelectInput(werte, werte.elementAt(0));
    ausfuehrungSuch.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        try
        {
          getZusatzbetraegeList();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });

    return ausfuehrungSuch;
  }

  public void handleStore()
  {
    try
    {
      Zusatzbetrag z = getZusatzbetrag();
      z.setFaelligkeit((Date) getFaelligkeit().getValue());
      z.setStartdatum((Date) getStartdatum(false).getValue());
      IntervallZusatzzahlung iz = (IntervallZusatzzahlung) getIntervall()
          .getValue();
      z.setIntervall(iz.getKey());
      z.setEndedatum((Date) getEndedatum().getValue());
      z.setBuchungstext((String) getBuchungstext().getValue());
      z.setBuchungstext2((String) getBuchungstext2().getValue());
      Double d = (Double) getBetrag().getValue();
      z.setBetrag(d.doubleValue());
      z.store();
      GUI.getStatusBar().setSuccessText("Zusatzbetrag gespeichert");
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Zusatzbetrages";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getZusatzbetraegeList() throws RemoteException
  {
    DBIterator zusatzbetraege = getIterator();

    if (zusatzbetraegeList == null)
    {
      zusatzbetraegeList = new TablePart(zusatzbetraege,
          new ZusatzbetraegeAction(null));
      zusatzbetraegeList.addColumn("Name", "mitglied", new Formatter()
      {

        @Override
        public String format(Object o)
        {
          Mitglied m = (Mitglied) o;
          if (m == null)
          {
            return null;
          }
          String name = null;
          try
          {
            name = Adressaufbereitung.getNameVorname(m);
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler", e);
          }
          return name;
        }
      });
      zusatzbetraegeList.addColumn("Startdatum", "startdatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("nächste Fälligkeit", "faelligkeit",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("letzte Ausführung", "ausfuehrung",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("Intervall", "intervalltext");
      zusatzbetraegeList.addColumn("Endedatum", "endedatum", new DateFormatter(
          new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("Buchungstext 1", "buchungstext");
      zusatzbetraegeList.addColumn("Buchungstext 2", "buchungstext2");
      zusatzbetraegeList.addColumn("Betrag", "betrag", new CurrencyFormatter(
          "", Einstellungen.DECIMALFORMAT));
      zusatzbetraegeList.addColumn("aktiv", "aktiv", new JaNeinFormatter());
      zusatzbetraegeList.setContextMenu(new ZusatzbetraegeMenu(
          zusatzbetraegeList));
      zusatzbetraegeList.setRememberColWidths(true);
      zusatzbetraegeList.setRememberOrder(true);
      zusatzbetraegeList.setSummary(true);
    }
    else
    {
      zusatzbetraegeList.removeAll();
      while (zusatzbetraege.hasNext())
      {
        zusatzbetraegeList.addItem(zusatzbetraege.next());
      }
    }
    if (this.ausfuehrungSuch.getText().equals("Aktive"))
    {
      nichtAktiveEliminieren(zusatzbetraegeList);
    }
    return zusatzbetraegeList;
  }

  private DBIterator getIterator() throws RemoteException
  {
    DBIterator zusatzbetraege = Einstellungen.getDBService().createList(
        Zusatzbetrag.class);
    zusatzbetraege.join("mitglied");
    zusatzbetraege.addFilter("zusatzabbuchung.mitglied = mitglied.id");
    if (this.ausfuehrungSuch.getText().equals("Alle"))
    {
      // nichts tun
    }
    else if (this.ausfuehrungSuch.getText().equals("Aktive"))
    {
      // zunächst nichts tun
    }
    else if (this.ausfuehrungSuch.getText().equals("Noch nicht ausgeführt"))
    {
      zusatzbetraege.addFilter("ausfuehrung is null");
    }
    else
    {
      try
      {
        Date d = new JVDateFormatTTMMJJJJ().parse(this.ausfuehrungSuch
            .getText());
        java.sql.Date sqd = new java.sql.Date(d.getTime());
        zusatzbetraege.addFilter("ausfuehrung = ?", new Object[] { sqd });
      }
      catch (ParseException e)
      {
        Logger.error("Fehler", e);
      }
    }
    zusatzbetraege
        .setOrder("ORDER BY ausfuehrung DESC, faelligkeit DESC, name");
    return zusatzbetraege;
  }

  private void nichtAktiveEliminieren(TablePart table) throws RemoteException
  {
    List<?> li = table.getItems();
    Iterator<?> it = li.iterator();
    while (it.hasNext())
    {
      Zusatzbetrag z = (Zusatzbetrag) it.next();
      if (!z.isAktiv())
      {
        table.removeItem(z);
      }
    }
  }

  public Button getPDFAusgabeButton()
  {
    Button b = new Button("PDF-Ausgabe", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          starteAuswertung();
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler beim Start der PDF-Ausgabe der Zusatzbeträge");
        }
      }
    }, null, true, "acroread.png");
    return b;
  }

  private void starteAuswertung() throws RemoteException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    String path = settings
        .getString("lastdir", System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname("zusatzbetraege", "", Einstellungen
        .getEinstellung().getDateinamenmuster(), "PDF").get());
    fd.setFilterExtensions(new String[] { "*.PDF" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.endsWith(".PDF"))
    {
      s = s + ".PDF";
    }
    final File file = new File(s);
    final DBIterator it = getIterator();
    settings.setAttribute("lastdir", file.getParent());
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          FileOutputStream fos = new FileOutputStream(file);
          Reporter reporter = new Reporter(fos, "Zusatzbeträge", "", it.size());
          reporter.addHeaderColumn("Mitglied", Element.ALIGN_LEFT, 60,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Startdatum", Element.ALIGN_LEFT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("nächste Fälligkeit", Element.ALIGN_LEFT,
              30, BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("letzte Ausführung", Element.ALIGN_LEFT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Intervall", Element.ALIGN_LEFT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Endedatum", Element.ALIGN_LEFT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Buchungstext", Element.ALIGN_LEFT, 50,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.createHeader();
          while (it.hasNext())
          {
            Zusatzbetrag z = (Zusatzbetrag) it.next();
            reporter.addColumn(
                Adressaufbereitung.getNameVorname(z.getMitglied()),
                Element.ALIGN_LEFT);
            reporter.addColumn(z.getStartdatum(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getFaelligkeit(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getAusfuehrung(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getIntervallText(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getEndedatum(), Element.ALIGN_LEFT);
            reporter.addColumn(
                z.getBuchungstext()
                    + (z.getBuchungstext2() != null
                        && z.getBuchungstext2().length() > 0 ? "\n"
                        + z.getBuchungstext() : ""), Element.ALIGN_LEFT);
            reporter.addColumn(z.getBetrag());
          }
          reporter.closeTable();
          reporter.close();
          fos.close();
          GUI.getCurrentView().reload();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          throw new ApplicationException(e);
        }
        FileViewer.show(file);
      }

      @Override
      public void interrupt()
      {
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(t);
  }
}