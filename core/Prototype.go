package core

import (
	"errors"
	"fmt"
)

type Prototype struct {
	Inherited *Prototype
	Body      map[string]any
}

var BasePrototype = Prototype{
	Inherited: nil,
	Body: map[string]any{
		"toString": func(vm *VM, self Prototype) (string, error) {
			return fmt.Sprintf("<prototype %p>", &self), nil
		},
	},
}

func NewPrototype(inherited *Prototype) *Prototype {
	return &Prototype{
		Inherited: inherited,
		Body:      make(map[string]any),
	}
}

func (p *Prototype) Set(name string, value any) {
	p.Body[name] = value
}

func (p *Prototype) Get(name string) (any, error) {
	if value, ok := p.Body[name]; ok {
		if val, ok := value.(OasisFunction); ok {
			val.Closure[0].Values["self"] = p
		}
		return value, nil
	} else if p.Inherited != nil {
		return p.Inherited.Get(name)
	}
	return nil, errors.New(fmt.Sprintf("Prototype does not contain %s", name))
}
