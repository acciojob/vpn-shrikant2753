package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{

        String countryNameCaps = countryName.toUpperCase();
        if (!countryNameCaps.equals("IND") && !countryNameCaps.equals("USA")
                && !countryNameCaps.equals("AUS") && !countryNameCaps.equals("CHI")
                && !countryNameCaps.equals("JPN"))
            throw new Exception("Country not found");

        Country country = new Country();
        country.setCountryName(CountryName.valueOf(countryNameCaps));
        country.setCode(CountryName.valueOf(countryNameCaps).toCode());

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setConnected(false);
        user.setMaskedIp(null);

        country.setUser(user);

        user.setOriginalCountry(country);

        userRepository3.save(user);
        user.setOriginalIp(user.getOriginalCountry().getCode()+"."+user.getId());

        userRepository3.save(user);

        return user;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user =userRepository3.findById(userId).get();
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();

        serviceProvider.getUsers().add(user);
        user.getServiceProviderList().add(serviceProvider);
        serviceProviderRepository3.save(serviceProvider);
        return user;
    }
}
