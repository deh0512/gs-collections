/*
 * Copyright 2014 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.list.fixed;

import java.util.Collections;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function0;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.api.list.FixedSizeList;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.map.MapIterable;
import com.gs.collections.api.stack.MutableStack;
import com.gs.collections.impl.Counter;
import com.gs.collections.impl.block.factory.Functions;
import com.gs.collections.impl.block.factory.Functions0;
import com.gs.collections.impl.factory.Lists;
import com.gs.collections.impl.list.Interval;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.stack.mutable.ArrayStack;
import com.gs.collections.impl.test.Verify;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractMemoryEfficientMutableListTestCase
{
    protected MutableList<String> list;

    @Before
    public void setUp()
    {
        this.list = this.classUnderTest();
    }

    protected MutableList<String> classUnderTest()
    {
        return Lists.fixedSize.ofAll(this.getNStrings());
    }

    private MutableList<String> getNStrings()
    {
        return Interval.oneTo(this.getSize()).collect(Functions.getToString()).toList();
    }

    protected abstract int getSize();

    protected abstract Class<?> getListType();

    @Test
    public void testGetClass()
    {
        Verify.assertInstanceOf(this.getListType(), this.list);
    }

    @Test
    public void sortThis()
    {
        Collections.shuffle(this.list);
        MutableList<String> sortedList = this.list.sortThis();
        Assert.assertSame(this.list, sortedList);
        Assert.assertEquals(
                this.getNStrings(),
                sortedList);
    }

    @Test
    public void sortThisBy()
    {
        Collections.shuffle(this.list);
        Assert.assertEquals(
                this.getNStrings(),
                this.list.sortThisBy(Functions.getStringToInteger()));
    }

    @Test
    public void reverseThis()
    {
        MutableList<String> expected = FastList.newList(this.list);
        MutableList<String> actual = this.list.reverseThis();
        Collections.reverse(expected);
        Assert.assertEquals(actual, expected);
        Assert.assertSame(this.list, actual);
    }

    @Test
    public void toReversed()
    {
        MutableList<String> actual = this.list.toReversed();
        MutableList<String> expected = FastList.newList(this.list).reverseThis();
        Assert.assertEquals(actual, expected);
        Assert.assertNotSame(this.list, actual);
    }

    @Test
    public void with()
    {
        MutableList<String> list = this.classUnderTest();
        Assert.assertFalse(list.contains("11"));
        MutableList<String> listWith = list.with("11");
        Assert.assertTrue(listWith.containsAll(list));
        Assert.assertTrue(listWith.contains("11"));
        Verify.assertInstanceOf(FixedSizeList.class, listWith);
    }

    @Test
    public void withAll()
    {
        MutableList<String> list = this.classUnderTest();
        Verify.assertContainsNone(list, "11", "12");
        MutableList<String> listWith = list.withAll(FastList.newListWith("11", "12"));
        Assert.assertTrue(listWith.containsAll(list));
        Verify.assertContainsAll(listWith, "11", "12");
        Verify.assertInstanceOf(FixedSizeList.class, listWith);
        Assert.assertSame(listWith, listWith.withAll(FastList.<String>newList()));
    }

    @Test
    public void withoutAll()
    {
        MutableList<String> list = this.classUnderTest().with("11").with("12");
        MutableList<String> listWithout = list.withoutAll(FastList.newListWith("11", "12"));
        Assert.assertTrue(listWithout.containsAll(this.classUnderTest()));
        Verify.assertContainsNone(listWithout, "11", "12");
        Verify.assertInstanceOf(FixedSizeList.class, listWithout);
        Assert.assertSame(listWithout, listWithout.withoutAll(FastList.<String>newList()));
    }

    @Test
    public void toStack()
    {
        MutableStack<String> stack = this.classUnderTest().toStack();
        Assert.assertEquals(ArrayStack.newStack(this.classUnderTest()), stack);
    }

    @Test
    public void aggregateByMutating()
    {
        Function<String, String> groupBy = Functions.getStringPassThru();
        Procedure2<Counter, String> sumAggregator = (aggregate, value) -> aggregate.add(Integer.parseInt(value));
        MapIterable<String, Counter> actual = this.classUnderTest().aggregateInPlaceBy(groupBy, Counter::new, sumAggregator);
        MapIterable<String, Counter> expected = FastList.newList(this.classUnderTest()).aggregateInPlaceBy(groupBy, Counter::new, sumAggregator);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void aggregateByNonMutating()
    {
        Function<String, String> groupBy = Functions.getStringPassThru();
        Function0<Integer> valueCreator = Functions0.value(0);
        Function2<Integer, String, Integer> sumAggregator = (aggregate, value) -> aggregate + Integer.parseInt(value);
        MapIterable<String, Integer> actual = this.classUnderTest().aggregateBy(groupBy, valueCreator, sumAggregator);
        MapIterable<String, Integer> expected = FastList.newList(this.classUnderTest()).aggregateBy(groupBy, valueCreator, sumAggregator);
        Assert.assertEquals(expected, actual);
    }
}
