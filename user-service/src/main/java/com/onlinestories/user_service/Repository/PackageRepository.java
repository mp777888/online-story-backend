package com.onlinestories.user_service.Repository;

import com.onlinestories.user_service.Entity.Package;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRepository extends MongoRepository<Package, String> {
}
