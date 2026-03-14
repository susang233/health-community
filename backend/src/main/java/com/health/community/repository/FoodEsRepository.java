package com.health.community.repository;

import com.health.community.entity.FoodDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface FoodEsRepository extends ElasticsearchRepository<FoodDoc, Long> {
    @Query("{ \"match\": { \"name\": { \"query\": \"?0\", \"fuzziness\": \"AUTO\" } } }")
    Page<FoodDoc> searchFuzzyByName(String name, Pageable pageable);


}
