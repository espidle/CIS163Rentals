package rentalproject;

import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public class ListModel extends AbstractTableModel {

    /**
     * holds all the rentals
     */
    private ArrayList<Rental> listOfRentals;

    /**
     * holds only the rentals that are to be displayed
     */
    private ArrayList<Rental> filteredListRentals;

    /**
     * current screen being displayed
     */
    private ScreenDisplay display = ScreenDisplay.CurrentRentalStatus;

    private String[] columnNamesCurrentRentals = {"Renter\'s Name", "Est. Cost",
            "Rented On", "Due Date ", "Console", "Name of the Game"};
    private String[] columnNamesEverythingStrings = {"Renter\'s Name", "Rented on Date", "Due Date", 
            "Actual Date Returned", "Est Cost", "Real Cost", "Console", "Name of Game"};
    private String[] columnNamesReturned = {"Renter\'s Name", "Rented On Date",
            "Due Date", "Actual date returned ", "Est. Cost", " Real Cost"};
    private String[] columnLateRentals = {"Renter\'s Name", "Est. Cost",
    "Rented On", "Due Date ", "Number of Days Late", "Console", "Name of the Game" };

    /* Formats all the dates used throughout the project **/
    private DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    private GregorianCalendar currentDate  = new GregorianCalendar();

    public ListModel() {
        display = ScreenDisplay.CurrentRentalStatus;
        listOfRentals = new ArrayList<>();
        filteredListRentals = new ArrayList<>();
        updateScreen();
        createList();
    }

    public void setDisplay(ScreenDisplay selected) {
        display = selected;
        updateScreen();
    }

    private void updateScreen() {
        switch (display) {
            case CurrentRentalStatus:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                        .filter(n -> n.actualDateReturned == null)
                        .collect(Collectors.toList());

                // Note: This uses Lambda function
                Collections.sort(filteredListRentals, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
                break;

            case ReturnedItems:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                       .filter(n -> n.actualDateReturned != null)
                       .collect(Collectors.toList());

                // Note: This uses an anonymous class.
                Collections.sort(filteredListRentals, new Comparator<Rental>() {
                    @Override
                    public int compare(Rental n1, Rental n2) {
                        return n1.nameOfRenter.compareTo(n2.nameOfRenter);
                    }
                });

                break;

            case DueWithInWeek:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream().filter( n -> {
                    GregorianCalendar tempRentedCal = new GregorianCalendar();
                    GregorianCalendar tempDueCal = new GregorianCalendar();
                    tempRentedCal = (GregorianCalendar)n.getRentedOn().clone();
                    tempDueCal = (GregorianCalendar)n.getDueBack().clone(); 
                    tempRentedCal.add(Calendar.DATE, 7);
                    return tempDueCal.compareTo(tempRentedCal) < 1;
                }).collect(Collectors.toList());
            
                break;

            case DueWithinWeekGamesFirst:
            ArrayList<Rental> gameObjects = new  ArrayList<Rental>();
            ArrayList<Rental> consoleObjects = new  ArrayList<Rental>();
            ArrayList<Rental> allObjects = new  ArrayList<Rental>();
                gameObjects = (ArrayList<Rental>) listOfRentals.stream().filter( n ->  n instanceof Game).collect(Collectors.toList());
                consoleObjects = (ArrayList<Rental>) listOfRentals.stream().filter( n ->  n instanceof Console).collect(Collectors.toList());
                Collections.sort(gameObjects, (n1,n2) -> n1.getNameOfRenter().compareTo(n2.getNameOfRenter()));
                Collections.sort(consoleObjects, (n1,n2) -> n1.getNameOfRenter().compareTo(n2.getNameOfRenter()));
                gameObjects.addAll(consoleObjects);
                allObjects = gameObjects;
                filteredListRentals = (ArrayList<Rental>) allObjects.stream().filter( n -> {
                    GregorianCalendar tempRentedCal = new GregorianCalendar();
                    GregorianCalendar tempDueCal = new GregorianCalendar();
                    tempRentedCal = (GregorianCalendar)n.getRentedOn().clone();
                    tempDueCal = (GregorianCalendar)n.getDueBack().clone(); 
                    tempRentedCal.add(Calendar.DATE, 7);
                    return tempDueCal.compareTo(tempRentedCal) < 1;
            }).collect(Collectors.toList());

                
                break;

            case Cap14DaysOverdue:
            filteredListRentals = (ArrayList<Rental>) listOfRentals.stream().filter(n -> {
                GregorianCalendar tempRentedCal = new GregorianCalendar();
                GregorianCalendar tempDueCal = new GregorianCalendar();
                tempRentedCal = (GregorianCalendar)n.getRentedOn().clone();
                tempDueCal = (GregorianCalendar)n.getDueBack().clone(); 
                tempRentedCal.add(Calendar.DATE, 7);
                return tempDueCal.compareTo(tempRentedCal) > -1; 
            }).map(n -> capGameOrConsole(n)
            ).collect(Collectors.toList());
            
             Collections.sort(filteredListRentals, (n1, n2) -> n1.getNameOfRenter().compareTo(n2.getNameOfRenter()));
           break;

           
            case EverythingScreen:
                filteredListRentals = listOfRentals;
                Collections.sort(filteredListRentals, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
                break;

            case LateRentals:
            ArrayList<Rental> gameLateObjects = new  ArrayList<Rental>();
            ArrayList<Rental> consoleLateObjects = new  ArrayList<Rental>();
            ArrayList<Rental> allLateObjects = new  ArrayList<Rental>();
                gameLateObjects = (ArrayList<Rental>) listOfRentals.stream().filter( n ->  n instanceof Game).collect(Collectors.toList());
                Collections.sort(gameLateObjects, new Comparator<Rental>() {
                    public int compare (Rental r1, Rental r2){
                        if(Integer.compare(r1.getDaysLate(), r2.getDaysLate()) == 0)
                            return r1.getNameOfRenter().compareTo(r2.getNameOfRenter());

                        return Integer.compare(r2.getDaysLate(), r1.getDaysLate());
                    }

                });
                consoleLateObjects = (ArrayList<Rental>) listOfRentals.stream().filter( n ->  n instanceof Console).collect(Collectors.toList());
                Collections.sort(consoleLateObjects, new Comparator<Rental>() {
                    public int compare (Rental r1, Rental r2){
                        if(Integer.compare(r1.getDaysLate(), r2.getDaysLate()) == 0)
                            return r1.getNameOfRenter().compareTo(r2.getNameOfRenter());

                        return Integer.compare(r2.getDaysLate(), r1.getDaysLate());
                    }

                });
                gameLateObjects.addAll(consoleLateObjects);
                allLateObjects = gameLateObjects;
                filteredListRentals = (ArrayList<Rental>) allLateObjects.stream().filter(n1 -> n1.actualDateReturned == null).collect(Collectors.toList());
                

                // Collections.sort(friends, new Comparator<Student>() {
                //     public int compare(Student o1, Student o2) {
                //         if(o1.getLastName().compareTo(o2.getLastName()) == 0) 
                //             return o1.getFirstName().compareTo(o2.getFirstName());
                //          else 
                //             return o1.getLastName().compareTo(o2.getLastName());
                //     }
                // });
                // System.out.println("TODO 4" + friends);
                break; 
            default:
                throw new RuntimeException("update is in undefined state: " + display);
        }
        fireTableStructureChanged();
    }
    
    /**
     * Private helper method to count the number of days between two
     * GregorianCalendar dates
     * Note that this is the proper way to do this; trying to use other
     * classes/methods likely won't properly account for leap days
     * @param startDate - the beginning/starting day
     * @param endDate - the last/ending day
     * @return int for the number of days between startDate and endDate
     */
    private int daysBetween(GregorianCalendar startDate, GregorianCalendar endDate) {
		// Determine how many days the Game was rented out
		GregorianCalendar gTemp = new GregorianCalendar();
		gTemp = (GregorianCalendar) endDate.clone(); //  gTemp = dueBack;  does not work!!
		int daysBetween = 0;
		while (gTemp.compareTo(startDate) > 0) {
			gTemp.add(Calendar.DATE, -1);                // this subtracts one day from gTemp
			daysBetween++;
		}

		return daysBetween;
	}
    private Rental capGameOrConsole(Rental R)
    {
        if(R instanceof Game)
        {
            Game temp = (Game)R; 
            Game G = new Game(temp.nameOfRenter, temp.rentedOn, temp.dueBack, temp.actualDateReturned, temp.getNameGame(), temp.getConsole());
            if(daysBetween(G.rentedOn, G.dueBack) >= 14)
                 G.setNameOfRenter(G.nameOfRenter.toUpperCase());
            return G;

        }
        else if(R instanceof Console)
        {
            Console temp = (Console) R; 
            Console C = new Console(temp.nameOfRenter, temp.rentedOn, temp.dueBack, temp.actualDateReturned, temp.getConsoleType());
            if(daysBetween(C.rentedOn, C.dueBack) >= 14)
                C.setNameOfRenter(C.nameOfRenter.toUpperCase()); 
            return C; 
        }
        else 
            return R;
        
    }

       @Override
    public String getColumnName(int col) {
        switch (display) {
            case CurrentRentalStatus:
                return columnNamesCurrentRentals[col];
            case ReturnedItems:
                return columnNamesReturned[col];
            case DueWithInWeek:
                return columnNamesCurrentRentals[col];
            case Cap14DaysOverdue:
                return columnNamesCurrentRentals[col];
            case DueWithinWeekGamesFirst:
                return columnNamesCurrentRentals[col];
            case EverythingScreen:
                return columnNamesEverythingStrings[col];
            case LateRentals:
                return columnLateRentals[col];

        }
        throw new RuntimeException("Undefined state for Col Names: " + display);
    }

    @Override
    public int getColumnCount() {
        switch (display) {
            case CurrentRentalStatus:
                return columnNamesCurrentRentals.length;
            case ReturnedItems:
                return columnNamesReturned.length;
            case DueWithInWeek:
                return columnNamesCurrentRentals.length;
            case Cap14DaysOverdue:
                return columnNamesCurrentRentals.length;
            case DueWithinWeekGamesFirst:
                return columnNamesCurrentRentals.length;
            case EverythingScreen: 
                return columnNamesEverythingStrings.length;
            case LateRentals:
                return columnLateRentals.length;



        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getRowCount() {
        return filteredListRentals.size();     // returns number of items in the arraylist
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (display) {
            case CurrentRentalStatus:
                return currentRentScreen(row, col);
            case ReturnedItems:
                return rentedOutScreen(row, col);
            case DueWithInWeek:
                return currentRentScreen(row, col);
            case Cap14DaysOverdue:
                return dueWithInWeekCap14(row, col);
            case DueWithinWeekGamesFirst:
                return dueWithInWeek(row, col);
            case EverythingScreen: 
                return everythingScreen(row, col);
            case LateRentals:
                return LateRentalsScreen(row, col);
        }
        throw new IllegalArgumentException();
    }

    private Object currentRentScreen(int row, int col) {
        switch (col) {
            case 0:
                return (filteredListRentals.get(row).nameOfRenter);

            case 1:
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                        get(row).dueBack));

            case 2:
                return (formatter.format(filteredListRentals.get(row).rentedOn.getTime()));

            case 3:
                if (filteredListRentals.get(row).dueBack == null)
                    return "-";

                return (formatter.format(filteredListRentals.get(row).dueBack.getTime()));

            case 4:
                if (filteredListRentals.get(row) instanceof Console)
                    return (((Console) filteredListRentals.get(row)).getConsoleType());
                else {
                    if (filteredListRentals.get(row) instanceof Game)
                        if (((Game) filteredListRentals.get(row)).getConsole() != null)
                            return ((Game) filteredListRentals.get(row)).getConsole();
                        else
                            return "";
                }
            
            case 5:
                if (filteredListRentals.get(row) instanceof Game)
                    return (((Game) filteredListRentals.get(row)).getNameGame());
                else
                    return "";
            default:
                throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }

    private Object LateRentalsScreen(int row, int col)
    {
        switch(col)
        {
        case 0:
            return (filteredListRentals.get(row).nameOfRenter);

        case 1:
            return (filteredListRentals.get(row).getCost(filteredListRentals.
                    get(row).dueBack));

        case 2:
            return (formatter.format(filteredListRentals.get(row).rentedOn.getTime()));

        case 3:
            if (filteredListRentals.get(row).dueBack == null)
                return "-";
            return (formatter.format(filteredListRentals.get(row).dueBack.getTime()));
        case 4: 
            return (filteredListRentals.get(row).getDaysLate());
            
        
        case 5: 
        if (filteredListRentals.get(row) instanceof Console)
            return (((Console) filteredListRentals.get(row)).getConsoleType());
        else {
            if (filteredListRentals.get(row) instanceof Game)
                if (((Game) filteredListRentals.get(row)).getConsole() != null)
                    return ((Game) filteredListRentals.get(row)).getConsole();
                else
                    return "";
            }

        case 6:
            if (filteredListRentals.get(row) instanceof Game)
                return (((Game) filteredListRentals.get(row)).getNameGame());
            else
                return "";
        default: 
         throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }
    private Object rentedOutScreen(int row, int col) {
        switch (col) {
            case 0:
                return (filteredListRentals.get(row).nameOfRenter);

            case 1:
                return (formatter.format(filteredListRentals.get(row).rentedOn.
                        getTime()));
            case 2:
                return (formatter.format(filteredListRentals.get(row).dueBack.
                        getTime()));
            case 3:
                return (formatter.format(filteredListRentals.get(row).
                        actualDateReturned.getTime()));

            case 4:
                return (filteredListRentals.
                        get(row).getCost(filteredListRentals.get(row).dueBack));

            case 5:
                return (filteredListRentals.
                        get(row).getCost(filteredListRentals.get(row).
                        actualDateReturned
                ));

            default:
                throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }
    private Object everythingScreen (int row, int col)
    {
        switch(col)
        {
            case 0:
                return (filteredListRentals.get(row).nameOfRenter);

            case 1:
                return (formatter.format(filteredListRentals.get(row).rentedOn.
                getTime()));
            case 2:
                return(formatter.format(filteredListRentals.get(row).dueBack.getTime()));
            case 3: 
                if(filteredListRentals.get(row).getActualDateReturned() != null){
                    return (formatter.format(filteredListRentals.get(row).actualDateReturned.getTime()));}
                else
                    return "N/A";
                    
            case 4:
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                get(row).dueBack));
            
            case 5:
                    if(filteredListRentals.get(row).actualDateReturned != null){
                return filteredListRentals.get(row).getCost(filteredListRentals.get(row).
                actualDateReturned);}
                    else{
                        return "Not Returned";}
            case 6:
                if (filteredListRentals.get(row) instanceof Console){
                    return (((Console) filteredListRentals.get(row)).getConsoleType());}
                else {
                    if (filteredListRentals.get(row) instanceof Game){
                        if (((Game) filteredListRentals.get(row)).getConsole() != null){
                            return ((Game) filteredListRentals.get(row)).getConsole();}
                        else{
                            return "";}
                    }
                }
            case 7:
                if (filteredListRentals.get(row) instanceof Game)
                    return (((Game) filteredListRentals.get(row)).getNameGame());
                else
                    return "";
            default:
                throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }

    private Object dueWithInWeek(int row, int col)
    {
        switch(col)
        {
            case 0:
                return (filteredListRentals.get(row).nameOfRenter);
            case 1:
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                get(row).dueBack));
            case 2:
                return (formatter.format(filteredListRentals.get(row).rentedOn.
                getTime()));
            case 3:
                return(formatter.format(filteredListRentals.get(row).dueBack.getTime()));
            case 4:
                if (filteredListRentals.get(row) instanceof Console){
                    return (((Console) filteredListRentals.get(row)).getConsoleType());}
                else {
                    if (filteredListRentals.get(row) instanceof Game){
                        if (((Game) filteredListRentals.get(row)).getConsole() != null){
                            return ((Game) filteredListRentals.get(row)).getConsole();}
                        else{
                            return "";}
                        }
                    }
            case 5:
                if (filteredListRentals.get(row) instanceof Game)
                    return (((Game) filteredListRentals.get(row)).getNameGame());
                else
                    return "";

            default:
                throw new RuntimeException("Row or col out of range:" + row + " " + col);
        }
    }

    private Object dueWithInWeekCap14(int row, int col)
    {
        switch(col){
        case 0:
                return (filteredListRentals.get(row).nameOfRenter);
            case 1:
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                get(row).dueBack));
            case 2:
                return (formatter.format(filteredListRentals.get(row).rentedOn.
                getTime()));
            case 3:
                return(formatter.format(filteredListRentals.get(row).dueBack.getTime()));
            case 4:
                if (filteredListRentals.get(row) instanceof Console){
                    return (((Console) filteredListRentals.get(row)).getConsoleType());}
                else {
                    if (filteredListRentals.get(row) instanceof Game){
                        if (((Game) filteredListRentals.get(row)).getConsole() != null){
                            return ((Game) filteredListRentals.get(row)).getConsole();}
                        else{
                            return "";}
                        }
                    }
            case 5:
                if (filteredListRentals.get(row) instanceof Game)
                    return (((Game) filteredListRentals.get(row)).getNameGame());
                else
                    return "";

            default:
                throw new RuntimeException("Row or col out of range:" + row + " " + col);
        }
    }
    public void add(Rental a) {
        listOfRentals.add(a);
        updateScreen();
    }

    public Rental get(int i) {
        return filteredListRentals.get(i);
    }

    public void update(int index, Rental unit) {
        updateScreen();
    }

    public void saveDatabase(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            System.out.println(listOfRentals.toString());
            os.writeObject(listOfRentals);
            os.close();
        } catch (IOException ex) {
            throw new RuntimeException("Saving problem! " + display);
        }
    }

    public void loadDatabase(String filename) {
        listOfRentals.clear();

        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream is = new ObjectInputStream(fis);
            listOfRentals = (ArrayList<Rental>) is.readObject();
            updateScreen();
            is.close();
        } catch (Exception ex) {
            throw new RuntimeException("Loading problem: " + display);

        }
    }

    public boolean saveAsText(String filename) {
        if (filename.equals("")) {
            throw new IllegalArgumentException();
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(filename)));
            out.println(listOfRentals.size());
            for (int i = 0; i < listOfRentals.size(); i++) {
                Rental unit = listOfRentals.get(i);
                out.println(unit.getClass().getName());
                out.println("Name is " + unit.getNameOfRenter());
                out.println("Rented on " + formatter.format(unit.rentedOn.getTime()));
                out.println("DueDate " + formatter.format(unit.dueBack.getTime()));

                if (unit.getActualDateReturned() == null)
                    out.println("Not returned!");
                else
                    out.println(formatter.format(unit.actualDateReturned.getTime()));

                if (unit instanceof Game) {
                    out.println(((Game) unit).getNameGame());
                    if (((Game) unit).getConsole() != null)
                        out.println(((Game) unit).getConsole());
                    else
                        out.println("No Console");
                }

                if (unit instanceof Console)
                    out.println(((Console) unit).getConsoleType());
            }
            out.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public void loadFromText(String filename) {
        listOfRentals.clear();
        if (filename.equals("")) {
            throw new IllegalArgumentException();
        }
        try{
            Scanner scan = new Scanner(new File(filename));
            int numOfRentals = Integer.parseInt(scan.nextLine());
            for(int i = 0; i < numOfRentals; i++)
            {
                String type = scan.nextLine(); 
                if(type.contains("Game"))
                {
                    String gameRenterName = scan.nextLine().substring(8);
                    String gameRentalDateString  = scan.nextLine().substring(10);
                    String gameDueDateString = scan.nextLine().substring(8);
                    String gameReturnStatString = scan.nextLine();
                    String gameTitle = scan.nextLine();
                    String gameConsoleName = scan.nextLine();

                    GregorianCalendar gameDueDate= new GregorianCalendar();
                    GregorianCalendar gameRentalDate = new GregorianCalendar();
                    GregorianCalendar gameReturnStat = new GregorianCalendar();
                    ConsoleTypes gamesConsole = ConsoleTypes.NoSelection;
                    
                    if(!(gameConsoleName.contains("No Console")))
                        gamesConsole = ConsoleTypes.valueOf(gameConsoleName);
                    if(gameReturnStatString.contains("Not returned!"))
                        {
                             gameReturnStat = null;
                        }
                    else
                    {
                        try{
                        Date tempDate = formatter.parse(gameReturnStatString);
                        gameReturnStat.setTime(tempDate);
                        }
                        catch(Exception ex)
                        {
                            System.out.println("return date problem");
                        }
                        
                    }
                    try{
                    Date tempD = formatter.parse(gameRentalDateString);
                    gameRentalDate.setTime(tempD);
                    tempD = formatter.parse(gameDueDateString);
                    gameDueDate.setTime(tempD);
                    }
                    catch(Exception ex)
                    {
                        System.out.println("Date not formatted correctly");
                    }
                   
                    Game g = new Game(gameRenterName, gameRentalDate, gameDueDate, gameReturnStat, gameTitle, gamesConsole);
                    listOfRentals.add(g);
                }
                else 
                {
                    String consoleRenterName = scan.nextLine().substring(8);
                    String consoleRentalDateString  = scan.nextLine().substring(10);
                    String consoleDueDateString = scan.nextLine().substring(8);
                    String consoleReturnStatString = scan.nextLine();
                    String consoleNameString = scan.nextLine();

                    GregorianCalendar consoleRentalDate = new GregorianCalendar();
                    GregorianCalendar consoleDueDate = new GregorianCalendar();
                    GregorianCalendar consoleReturnStat = new GregorianCalendar();
                    ConsoleTypes consoleName = ConsoleTypes.valueOf(consoleNameString);
                    if(consoleReturnStatString.contains("Not returned!"))
                    {
                        consoleReturnStat = null;
                    }
                    else
                    {
                        try{
                            Date tempD = formatter.parse(consoleReturnStatString);
                            consoleReturnStat.setTime(tempD);
                        }
                        catch(Exception ex){
                            System.out.println("Problem with parsing console  return date");
                        }
                    }
                    try {
                    Date tempDate = formatter.parse(consoleRentalDateString);
                    consoleRentalDate.setTime(tempDate);
                    tempDate = formatter.parse(consoleDueDateString);
                    consoleDueDate.setTime(tempDate);
                    }

                    catch(Exception ex)
                    {

                    }
                   
                    Console c = new Console(consoleRenterName, consoleRentalDate,consoleDueDate, consoleReturnStat, consoleName);
                    listOfRentals.add(c);
                }
                
            }
        }
        catch(IOException ex)
        {
            System.out.println("Cannot load file");
        }
        updateScreen();
    }

    /**********************************************************************
     *
     *  DO NOT MODIFY THIS METHOD!!!!!!
     */
    public void createList() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        GregorianCalendar g1 = new GregorianCalendar();
        GregorianCalendar g2 = new GregorianCalendar();
        GregorianCalendar g3 = new GregorianCalendar();
        GregorianCalendar g4 = new GregorianCalendar();
        GregorianCalendar g5 = new GregorianCalendar();
        GregorianCalendar g6 = new GregorianCalendar();
        GregorianCalendar g7 = new GregorianCalendar();
        GregorianCalendar g8 = new GregorianCalendar();

        try {
            Date d1 = df.parse("1/20/2020");
            g1.setTime(d1);
            Date d2 = df.parse("12/22/2020");
            g2.setTime(d2);
            Date d3 = df.parse("12/20/2019");
            g3.setTime(d3);
            Date d4 = df.parse("7/02/2020");
            g4.setTime(d4);
            Date d5 = df.parse("1/20/2010");
            g5.setTime(d5);
            Date d6 = df.parse("9/29/2020");
            g6.setTime(d6);
            Date d7 = df.parse("7/25/2020");
            g7.setTime(d7);
            Date d8 = df.parse("7/29/2020");
            g8.setTime(d8);

            Console console1 = new Console("Person1", g4, g6, null, ConsoleTypes.PlayStation4);
            Console console2 = new Console("Person2", g5, g3, null, ConsoleTypes.PlayStation4);
            Console console3 = new Console("Person5", g4, g8, null, ConsoleTypes.SegaGenesisMini);
            Console console4 = new Console("Person6", g4, g7, null, ConsoleTypes.SegaGenesisMini);
            Console console5 = new Console("Person1", g5, g4, g3, ConsoleTypes.XBoxOneS);

            Game game1 = new Game("Person1", g3, g2, null, "title1", ConsoleTypes.PlayStation4);
            Game game2 = new Game("Person1", g3, g1, null, "title2", ConsoleTypes.PlayStation4);
            Game game3 = new Game("Person1", g5, g3, null, "title2", ConsoleTypes.SegaGenesisMini);
            Game game4 = new Game("Person7", g4, g8, null, "title2", null);
            Game game5 = new Game("Person3", g3, g1, g1, "title2", ConsoleTypes.XBoxOneS);
            Game game6 = new Game("Person6", g4, g7, null, "title1", ConsoleTypes.NintendoSwitch);
            Game game7 = new Game("Person5", g4, g8, null, "title1", ConsoleTypes.NintendoSwitch);

            add(game1);
            add(game4);
            add(game5);
            add(game2);
            add(game3);
            add(game6);
            add(game7);

            add(console1);
            add(console2);
            add(console5);
            add(console3);
            add(console4);

            // create a bunch of them.
            int count = 0;
            Random rand = new Random(13);
            String guest = null;

            while (count < 300) {  // change this number to 300 for a complete test of your code
                Date date = df.parse("7/" + (rand.nextInt(10) + 2) + "/2020");
                GregorianCalendar g = new GregorianCalendar();
                g.setTime(date);
                if (rand.nextBoolean()) {
                    guest = "Game" + rand.nextInt(5);
                    Game game;
                    if (count % 2 == 0)
                        game = new Game(guest, g4, g, null, "title2", ConsoleTypes.NintendoSwitch);
                    else
                        game = new Game(guest, g4, g, null, "title2", null);
                        game.setDaysLate(daysBetween(game.dueBack, currentDate));
                    add(game);


                } else {
                    guest = "Console" + rand.nextInt(5);
                    date = df.parse("7/" + (rand.nextInt(20) + 2) + "/2020");
                    g.setTime(date);
                    Console console = new Console(guest, g4, g, null, getOneRandom(rand));
                    console.setDaysLate(daysBetween(console.dueBack, currentDate));
                    add(console);
                }

                count++;
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error in testing, creation of list");
        }
    }

    public ConsoleTypes getOneRandom(Random rand) {

        int number = rand.nextInt(ConsoleTypes.values().length - 1);
        switch (number) {
            case 0:
                return ConsoleTypes.PlayStation4;
            case 1:
                return ConsoleTypes.XBoxOneS;
            case 2:
                return ConsoleTypes.PlayStation4Pro;
            case 3:
                return ConsoleTypes.NintendoSwitch;
            default:
                return ConsoleTypes.SegaGenesisMini;
        }
    }
}