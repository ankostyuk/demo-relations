package ru.nullpointer.nkbrelation.support;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.util.Assert;

/**
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public class PaginatedQueryResult<T> implements Iterable<T> {

    private final PageConfig pageConfig;
    private final List<T> list;
    private final long total;

    /**
     * Результат постраничной выборки. Нумерация страниц с ЕДИНИЦЫ.
     *
     * @param pageConfig настройки постраничной выборки
     * @param list Список результатов на странице. Может быть пустым
     * @param total Общее количество результатов на всех страницах. Может
     * равняться нулю
     */
    public PaginatedQueryResult(PageConfig pageConfig, List<T> list, long total) {
        Assert.notNull(pageConfig);
        Assert.notNull(list);
        Assert.isTrue(total >= 0);

        this.pageConfig = pageConfig;
        this.list = list;
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public long getTotal() {
        return total;
    }

    public int getPageNumber() {
        return pageConfig.getPageNumber();
    }

    public int getPageSize() {
        return pageConfig.getPageSize();
    }

    public long getPageCount() {
        int pageSize = pageConfig.getPageSize();
        return ((total / pageSize) + (total % pageSize > 0 ? 1 : 0));
    }

    public int getFirstNumber() {
        int pageNumber = pageConfig.getPageNumber();
        return getPageCount() >= pageNumber ? (pageNumber - 1) * getPageSize() + 1 : 0;
    }

    public int getLastNumber() {
        int pageNumber = pageConfig.getPageNumber();
        return getPageCount() >= pageNumber ? (pageNumber - 1) * getPageSize() + list.size() : 0;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
