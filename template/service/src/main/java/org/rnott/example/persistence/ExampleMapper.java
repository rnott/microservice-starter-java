package org.rnott.example.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.rnott.example.api.Example;
import org.rnott.example.api.PageOfExamples;

@Mapper(uses = ExampleMapper.class)
public interface ExampleMapper extends AbstractEntityMapper<Example, PageOfExamples, ExampleEntity> {
    ExampleMapper INSTANCE = Mappers.getMapper(ExampleMapper.class);

}
