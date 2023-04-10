package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        User user = userRepository2.findById(userId).get();
        if(user.getMaskedIp().equals(null)){
            throw  new Exception("Already connected");
        }

        else if(countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())){
            return user;
        }
        else{
            if(user.getServiceProviderList().size()==0){
                throw new Exception("Unable to connect");
            }

            List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
            int a = Integer.MAX_VALUE;
            ServiceProvider serviceProvider=null;
            Country country = null;

            for(ServiceProvider serviceProvider1 : serviceProviderList){
                List<Country> countryList = serviceProvider1.getCountryList();

                for(Country country1 : countryList){
                    if(countryName.equalsIgnoreCase(country1.getCountryName().toString()) &&
                            a > serviceProvider1.getId()){
                        a = serviceProvider1.getId();
                        serviceProvider = serviceProvider1;
                        country = country1;
                    }
                }
            }

            if (serviceProvider!=null){
                Connection connection = new Connection();
                connection.setUser(user);
                connection.setServiceProvider(serviceProvider);

                String cc = country.getCode();
                int givenId = serviceProvider.getId();
                String mask = cc+"."+givenId+"."+userId;

                user.setMaskedIp(mask);
                user.setConnected(true);
                user.getConnectionList().add(connection);

                serviceProvider.getConnectionList().add(connection);

                userRepository2.save(user);
                serviceProviderRepository2.save(serviceProvider);
            }
        }
        return  user;
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
