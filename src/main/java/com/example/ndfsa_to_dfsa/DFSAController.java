package com.example.ndfsa_to_dfsa;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class DFSAController {
    HashMap<String, States> table = new HashMap<String, States>();
    ArrayList<String> states = new ArrayList<>();
    HashMap<String, States> feasiblePairs = new HashMap<String, States>();
    ArrayList<String> feasiblePairsName = new ArrayList<>();
    States Start;


    @FXML
    TextArea transitionTable;

    @FXML
    TextField checkString;

    @FXML
    Label invisibleLabel;

    @FXML
    protected void onBrowseBtnClk() throws IOException {
        ReadFile();

        for (int i=0;i<states.size();i++){
            if (table.get(states.get(i))!=null){
                transitionTable.appendText("\n state: "+states.get(i)+"\n");
                transitionTable.appendText("\n transition: ");

                table.get(states.get(i)).getStates_trans().entrySet().forEach(entry->{
                    transitionTable.appendText("\n\t");
                    transitionTable.appendText(entry.getKey()+" --> "+entry.getValue());
                });
                transitionTable.appendText("\n"+"------------------------------------------------------");
            }
        }
    }

    /*
    Read a file and save the states in hashmap in save transition enter hashmap also
     */
    public void ReadFile() throws IOException {
        FileChooser fC=new FileChooser();
        File file = fC.showOpenDialog(new Stage());
        BufferedReader br = new BufferedReader(new FileReader(file.getPath()));

        String First_Line = br.readLine();
        String[] states_inFile = First_Line.split(" ");

        for (int i = 0; i < states_inFile.length; i++) {
            states.add(states_inFile[i]);
        }

        for (int i = 0; i < states.size(); i++) {
            HashMap<String, String> inner = new HashMap<>();
            States state = new States(states.get(i), false);
            state.setStates_trans(inner);
            table.put(states.get(i), state);
        }
        Start=table.get(states.get(0));
        String FinaState = br.readLine();
        table.get(FinaState).setFinal_stat(true);

        String Third_Line = br.readLine();
        String[] transition = Third_Line.split(" ");

        String line = br.readLine();
        while (line != null) {
            if (!line.isEmpty()) {
                String[] states_trans = line.split(" ");
                States inner = table.get(states_trans[0]);
                String val = inner.getStates_trans().get(states_trans[2]);
                if (val != null) {
                    table.get(states_trans[0]).getStates_trans().put(states_trans[2], val.concat(",").concat(states_trans[1]));

                } else
                    table.get(states_trans[0]).getStates_trans().put(states_trans[2], states_trans[1]);

            }
            line = br.readLine();
        }



        Removal_Lambda_Transitions(transition, FinaState);


        Removal_Of_NonDeterminism(transition);

        Removal_of_Non_Accessible_States(transition);

        Merging_Equivalent_States(transition);


    }

    public void Removal_Lambda_Transitions(String[] trans, String Final_state) {
        //checks all states starting form S
        for (int i = 0; i < states.size(); i++) {
            //get the states in transition lambda for each state
            String indexOfLambda = table.get(states.get(i)).getStates_trans().get("@");

            //if there are state in lambda transition
            if (indexOfLambda != null) {
                //if there are multiple states in lambda
                String[] vars = indexOfLambda.split(",");

                //check each state in lambda
                for (int j = 0; j < vars.length; j++) {

                    //if there is one is final in lambda the state will be also final
                    if (vars[j].equals(Final_state)) {
                        table.get(states.get(i)).setFinal_stat(true);
                    }

                    //get the transition for each state
                    HashMap<String, String> temp = table.get(vars[j]).getStates_trans();
                    for (int k = 0; k < trans.length; k++) {
                        if (temp.get(trans[k]) != null) {
                            String val = table.get(states.get(i)).getStates_trans().get(trans[k]);
                            if (val != null)
                                val = val.concat(",").concat(temp.get(trans[k]));
                            else
                                val = temp.get(trans[k]);
                            table.get(states.get(i)).getStates_trans().put(trans[k], val);
                        }
                    }
                    if (j == vars.length - 1) {
                        indexOfLambda = table.get(states.get(i)).getStates_trans().get("@");
                        String[] vars2 = indexOfLambda.split(",");
                        if (vars2.length > vars.length) {
                            vars = vars2;
                        }
                    }
                }

            }
            table.get(states.get(i)).getStates_trans().remove("@");
        }
    }

    /*
    step 2 to convert for DFSA is this Removal of nondeterminism
    via check each trans in the state if have more than one state for the same transition
     */
    public void Removal_Of_NonDeterminism(String[] trans) {
        for (int j = 0; j < states.size(); j++) {
            States state = table.get(states.get(j));
            HashMap<String, String> HashState = state.getStates_trans();
              HashState.entrySet().forEach(entry1 -> {
                String indexTransition = entry1.getValue();

                if (indexTransition != null) {
                    String[] arrayIndex = indexTransition.split(",");
                    if (arrayIndex.length > 1 && table.get(indexTransition) == null) {
                        boolean flag = false;
                        HashMap<String, String> HashNew = new HashMap<String, String>();
                        for (int i = 0; i < arrayIndex.length; i++) {
                            HashMap<String, String> temp = table.get(arrayIndex[i]).getStates_trans();
                            if (table.get(arrayIndex[i]).isFinal_stat(true))
                                flag = true;
                            for (int k = 0; k < trans.length; k++) {
                                if (temp.get(trans[k]) != null) {
                                    String temp_trans = HashNew.get(trans[k]);
                                    if (temp_trans == null)
                                        HashNew.put(trans[k], temp.get(trans[k]));
                                    else
                                        HashNew.put(trans[k], temp_trans.concat(",").concat(temp.get(trans[k])));
                                }
                            }
                        }

                        States NewState = new States(indexTransition, flag);
                        NewState.setStates_trans(HashNew);
                        table.put(indexTransition, NewState);
                        states.add(indexTransition);
                    }


                }

            });

        }

        Rename(trans);

    }

    /*
    Rename the state which already new and have multiple state in this name
     */
    public void Rename(String[] trans){
        for (int i=0;i<states.size();i++){
            States state=table.get(states.get(i));
            if (table.get(states.get(i)).getState().length()>1){
                String[] indexOfHash=table.get(states.get(i)).getState().split(",");
                String Name="";
                for (int k=0;k<indexOfHash.length;k++)
                    Name = Name.concat(indexOfHash[k]);
                table.remove(state.getState());
                state.setState(Name);
               table.put(Name,state);
                states.set(i,Name);
            }

        }

        for (int i=0;i<states.size();i++){
            HashMap<String,String> index=table.get(states.get(i)).getStates_trans();

            for (int j=0;j<trans.length;j++){
                String indexTrans=index.get(trans[j]);
                if(indexTrans!=null && indexTrans.length()>1){
                    String[] arrayIndex=indexTrans.split(",");
                    String NewName="";
                    for (int k=0;k<arrayIndex.length;k++)
                        NewName = NewName.concat(arrayIndex[k]);
                    table.get(states.get(i)).getStates_trans().put(trans[j],NewName);
                }

            }
        }
    }

    /*
    step 3 to DFSA is Removal of non-accessible states
     */
    public void Removal_of_Non_Accessible_States(String[] trans) {
        table.get(states.get(0)).setMarked(true);
        Recursion_Accessible(table.get(states.get(0)), trans);

        for (int i = 0; i < states.size(); i++) {
            try {
                if (!table.get(states.get(i)).isMarked()) {
                    table.remove(states.get(i));
                }
            }
            catch (Exception ex){
                System.out.println("Here Exception: "+ex);
            }

        }


    }

    /*
    this recursion to ensure that checked all state again and again
     */
    public void Recursion_Accessible(States s, String[] trans) {
        int count = 0;
        while (count != trans.length) {
            if (s.getStates_trans().get(trans[count]) != null && !table.get(s.getStates_trans().get(trans[count])).isMarked()) {
                table.get(s.getStates_trans().get(trans[count])).setMarked(true);
                Recursion_Accessible(table.get(s.getStates_trans().get(trans[count])), trans);
            } else
                count++;
        }
    }

    /*
    final step for DFSA to merge all equivalent state
     */
    public void Merging_Equivalent_States(String[] trans) {
        ArrayList<States> Final = new ArrayList<>();
        ArrayList<States> Non_Final = new ArrayList<>();

        for (int i = 0; i < states.size(); i++) {
            if (table.get(states.get(i)) != null) {
                if (table.get(states.get(i)).isFinal_stat()) {
                    Final.add(table.get(states.get(i)));
                } else
                    Non_Final.add(table.get(states.get(i)));
            }
        }


        for (int i = 0; i < Non_Final.size(); i++) {
            for (int j = i + 1; j < Non_Final.size(); j++) {
                boolean flag = false;
                for (int k = 0; k < trans.length; k++) {
                    if ((Non_Final.get(i).getStates_trans().get(trans[k]) != null
                            && Non_Final.get(j).getStates_trans().get(trans[k]) != null) || (Non_Final.get(i).getStates_trans().get(trans[k]) == null
                            && Non_Final.get(j).getStates_trans().get(trans[k]) == null)) {
                        flag = true;

                    } else {
                        flag = false;
                        break;
                    }

                }
                if (flag) {
                    String firstStateName = Non_Final.get(i).getState();
                    String secondStateName = Non_Final.get(j).getState();
                    String Both = firstStateName.concat(",").concat(secondStateName);
                    States state = new States(Both, false);
                    for (int index = 0; index < trans.length; index++) {
                        if (Non_Final.get(i).getStates_trans().get(trans[index]) != null) {
                            String first = Non_Final.get(i).getStates_trans().get(trans[index]);
                            String second = Non_Final.get(j).getStates_trans().get(trans[index]);
                            state.getStates_trans().put(trans[index], first.concat(",").concat(second));
                        }
                    }
                    feasiblePairs.put(Both, state);
                    feasiblePairsName.add(Both);
                }
            }
        }

        for (int i = 0; i < Final.size(); i++) {
            for (int j = i + 1; j < Final.size(); j++) {
                boolean flag = false;
                for (int k = 0; k < trans.length; k++) {
                    if ((Final.get(i).getStates_trans().get(trans[k]) != null
                            && Final.get(j).getStates_trans().get(trans[k]) != null) || (Final.get(i).getStates_trans().get(trans[k]) == null
                            && Final.get(j).getStates_trans().get(trans[k]) == null)) {
                        flag = true;

                    } else {
                        flag = false;
                        break;
                    }

                }
                if (flag) {
                    String firstStateName = Final.get(i).getState();
                    String secondStateName = Final.get(j).getState();
                    String Both = firstStateName.concat(",").concat(secondStateName);
                    States state = new States(Both, false);
                    for (int index = 0; index < trans.length; index++) {
                        if (Final.get(i).getStates_trans().get(trans[index]) != null) {
                            String first = Final.get(i).getStates_trans().get(trans[index]);
                            String second = Final.get(j).getStates_trans().get(trans[index]);
                            state.getStates_trans().put(trans[index], first.concat(",").concat(second));
                        }
                    }
                    feasiblePairs.put(Both, state);
                    feasiblePairsName.add(Both);
                }
            }

        }


        for (int i = 0; i < feasiblePairsName.size(); i++) {
            Recursion_Equivalent(trans, feasiblePairs.get(feasiblePairsName.get(i)));
        }

        for (int i = 0; i < feasiblePairsName.size(); i++) {
              if (!feasiblePairs.get(feasiblePairsName.get(i)).isMarked()){

              }
        }



        Merging(trans);



    }


     /*
      this recursion to ensure that all feasible pairs checked again and again
     */
    public void Recursion_Equivalent(String[] trans, States feasiblePairState) {
        feasiblePairs.get(feasiblePairState.getState()).setVisited(true);
        int count = 0;
        while (count != trans.length) {
            if (feasiblePairState.getStates_trans().get(trans[count]) != null) {
                String name = feasiblePairState.getStates_trans().get(trans[count]);
                String[] index = name.split(",");
                String SwapName = index[1].concat(",").concat(index[0]);
                if (index[0].equals(index[1])) {
                    count++;
                    continue;
                }

                String NewName="";
                if(feasiblePairs.get(name) != null)
                    NewName=name;
                else if(feasiblePairs.get(SwapName) != null)
                    NewName=SwapName;

                if (!NewName.isEmpty()&&feasiblePairs.get(NewName)!=null) {
                    if (feasiblePairs.get(NewName).isMarked()) {
                        feasiblePairs.get(feasiblePairState.getState()).setMarked(true);

                        break;
                    } else if(!feasiblePairState.getStates_trans().get(trans[count]).equals(feasiblePairState.getState())&& !feasiblePairs.get(feasiblePairState.getStates_trans().get(trans[count])).isVisited()){
                        Recursion_Equivalent(trans, feasiblePairs.get(NewName));

                        }
                    }
                else{
                    feasiblePairs.get(feasiblePairState.getState()).setMarked(true);
                    break;
                }
            }
            count++;
        }

    }

    /*
    to check if their equivalent will merge it
     */
    public void Merging(String[] trans){
        HashMap <String, LinkedList<String>> hashOfLinkedList=new HashMap<>();
        HashMap <String, LinkedList<String>> Sec_hashOfLinkedList=new HashMap<>();
        ArrayList<String> NameOfIndex=new ArrayList<>();
        for (int i=0;i<feasiblePairsName.size();i++){
              States state=feasiblePairs.get(feasiblePairsName.get(i));

              if (!state.isMarked()){
                  String[] index=state.getState().split(",");
                  if (hashOfLinkedList.size()==0 && Sec_hashOfLinkedList.size()==0){
                      LinkedList<String> newLinkedList=new LinkedList<>();
                      LinkedList<String> Sec_newLinkedList=new LinkedList<>();

                      newLinkedList.add(index[1]);
                      hashOfLinkedList.put(index[0],newLinkedList);

                      Sec_newLinkedList.add(index[0]);
                      Sec_hashOfLinkedList.put(index[1],Sec_newLinkedList);
                  }


                  if (hashOfLinkedList.get(index[0])==null){
                      if (Sec_hashOfLinkedList.get(index[0])==null){
                          LinkedList<String> newLinkedList=new LinkedList<>();
                          newLinkedList.add(index[1]);
                          hashOfLinkedList.put(index[0],newLinkedList);
                          NameOfIndex.add(index[0]);
                      }
                      else{
                          Sec_hashOfLinkedList.get(index[0]).add(index[1]);
                      }
                  }
                  else
                      hashOfLinkedList.get(index[0]).add(index[1]);


                  if (Sec_hashOfLinkedList.get(index[1])==null){
                      if (hashOfLinkedList.get(index[1])==null){
                          LinkedList<String> newLinkedList=new LinkedList<>();
                          newLinkedList.add(index[0]);
                          Sec_hashOfLinkedList.put(index[1],newLinkedList);
                      }
                      else{
                          hashOfLinkedList.get(index[1]).add(index[0]);
                      }
                  }
                  else
                      Sec_hashOfLinkedList.get(index[1]).add(index[0]);

              }
        }


        for (int i=0;i<NameOfIndex.size();i++){
            LinkedList<String> temp=hashOfLinkedList.get(NameOfIndex.get(i));
            for (int j=0;j<temp.size();j++){
                if(hashOfLinkedList.get(temp.get(j))!=null){
                    LinkedList<String> Sec_temp=hashOfLinkedList.get(temp.get(j));
                    for (int k=0;k< Sec_temp.size();k++){
                        if (!Sec_temp.get(j).equals(NameOfIndex.get(i)) && (hashOfLinkedList.get(Sec_temp.get(j))!=null || Sec_hashOfLinkedList.get(Sec_temp.get(j))!=null)){
                            hashOfLinkedList.get(NameOfIndex.get(i)).add(Sec_temp.get(j));
                        }
                    }
                    hashOfLinkedList.remove(temp.get(j));

                }

                else if(Sec_hashOfLinkedList.get(temp.get(j))!=null){
                    LinkedList<String> Sec_temp=Sec_hashOfLinkedList.get(temp.get(j));
                    for (int k=0;k< Sec_temp.size();k++){
                        if (!Sec_temp.get(j).equals(NameOfIndex.get(i)) && (hashOfLinkedList.get(Sec_temp.get(j))!=null || Sec_hashOfLinkedList.get(Sec_temp.get(j))!=null)){
                            hashOfLinkedList.get(NameOfIndex.get(i)).add(Sec_temp.get(j));
                        }
                    }
                    Sec_hashOfLinkedList.remove(temp.get(j));
                }
            }
        }



         hashOfLinkedList.entrySet().forEach(entry -> {
            LinkedList<String> temp=entry.getValue();
            for (int j=0;j<temp.size();j++){
                System.out.println(temp.get(j));
                for (int k=0;k<states.size();k++){
                    if (table.get(states.get(k))!=null) {
                        if(table.get(states.get(k)).getState().equals(temp.get(j)))
                        table.remove(states.get(k));
                        else{
                        HashMap<String,String> transHash=table.get(states.get(k)).getStates_trans();
                        for (int m=0;m<trans.length;m++){
                            if (transHash.get(trans[m])!=null && transHash.get(trans[m]).equals(temp.get(j))){
                                transHash.put(trans[m],entry.getKey());
                            }
                        }
                    }}
                }

            }

        });


    }

    public void printChecked(){
        if (CheckAcceptedString(checkString)){
            invisibleLabel.setText("Is Accepted String");
        }
        else
            invisibleLabel.setText("Is rejected string");
    }

    /*
    check if string accepted or not
     */
    public boolean CheckAcceptedString(TextField checkString){
        String UserString=checkString.getText();
        States state=Start;
        for (int i=0;i<UserString.length();i++){
            if (Character.isDigit(UserString.charAt(i))){
                UserString=UserString.replace(UserString.charAt(i),'d');

            }
            String trans=Character.toString(UserString.charAt(i));

            if(i==UserString.length()-1){
                if (state.getStates_trans().get(trans)!=null&&table.get(state.getStates_trans().get(trans)).isFinal_stat())
                    return true;
                else
                    return false;

            }

            else if (state.getStates_trans().get(trans)!=null){
                state=table.get(state.getStates_trans().get(trans));
            }
            else return false;


        }
        return true;


    }



}