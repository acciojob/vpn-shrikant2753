package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        countryName = countryName.toUpperCase();
        User user = userRepository2.findById(userId).get();
        if (user.getConnected() || user.getMaskedIp() != null) {
            throw new Exception("Already connected");
        }
        String currentCountry = String.valueOf(user.getOriginalCountry().getCountryName());
        if (currentCountry.equals(countryName)) {
            return user;
        }

        List<ServiceProvider> updatedServiceProvider = new ArrayList<>();
        boolean marker = true;
        ServiceProvider realProvider = null;
        int max = Integer.MAX_VALUE;
        Country country = null;
        List<ServiceProvider> userServiceProviderList = user.getServiceProviderList();

        for (ServiceProvider s : userServiceProviderList) {
            List<Country> countryList = s.getCountryList();
            for (Country c : countryList) {
                if (c.getCountryName().toString().equals(countryName) && max > s.getId()) {
                    max = s.getId();
                    realProvider = s;
                    country = c;
                    marker = false;
                }
            }
        }
        if (marker) {
            throw new Exception("Unable to connect");
        }

        Connection connection = new Connection();
        connection.setUser(user);
        user.setConnected(true);
        List<Connection> connectionList = user.getConnectionList();
        connectionList.add(connection);
        user.setConnectionList(connectionList);
        connection.setServiceProvider(realProvider);


        user.setMaskedIp(country.getCode() + "." + realProvider.getId() + "." + user.getId());

        realProvider.getConnectionList().add(connection);

        serviceProviderRepository2.save(realProvider);
        userRepository2.save(user);

        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();

        if(user.getConnected()==false){
            throw new Exception("Already disconnected");
        }
        user.setMaskedIp(null);
        user.setConnected(false);
        userRepository2.save(user);
        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User receiver = userRepository2.findById(receiverId).get();
        CountryName receiverCountryName = null;
        if (receiver.getConnected()) {
            String maskedCode = receiver.getMaskedIp().substring(0, 3);
            switch (maskedCode) {
                case "001":
                    receiverCountryName = CountryName.IND;
                    break;
                case "002":
                    receiverCountryName = CountryName.USA;
                    break;
                case "003":
                    receiverCountryName = CountryName.AUS;
                    break;
                case "004":
                    receiverCountryName = CountryName.CHI;
                    break;
                case "005":
                    receiverCountryName = CountryName.JPN;
                    break;

            }
        } else {
            receiverCountryName = receiver.getOriginalCountry().getCountryName();
        }

        User user = null;
        try {
            user = connect(senderId, receiverCountryName.toString());
        } catch (Exception e) {
            throw new Exception("Cannot establish communication");
        }
        return user;
    }
}
