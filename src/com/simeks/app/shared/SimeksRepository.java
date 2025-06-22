package com.simeks.app.shared;

import com.simeks.app.shared.model.Ekspedisi;
import com.simeks.app.shared.model.Logbook;
import com.simeks.app.shared.model.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import com.simeks.app.shared.model.*;

public interface SimeksRepository extends Remote {
    User login(String username, String password) throws RemoteException;
    Map<String, Integer> getAdminDashboardStats() throws RemoteException;
    List<User> getAllUsers() throws RemoteException;
    Ekspedisi getActiveEkspedisiForUser(int userId) throws RemoteException;
    void submitLogbook(Logbook log) throws RemoteException;
}